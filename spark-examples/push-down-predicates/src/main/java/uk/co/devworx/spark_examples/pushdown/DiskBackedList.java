package uk.co.devworx.spark_examples.pushdown;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A Java List that is backed by an H2 Database - useful in
 * passing things over the serialization boundery.
 */
public class DiskBackedList<E> implements List<E>, Serializable, Closeable
{
	private final String fileName;

	private transient volatile Connection con;

	public DiskBackedList(String filename)
	{
		this.fileName = filename;
	}

	Connection getConnection()
	{
		if(con != null)
		{
			return con;
		}
		if(con == null)
		{
			synchronized (this)
			{
				if(con != null)
				{
					return con;
				}

				try
				{   String secret = "23pod23rkfsdvjc2+ewqe1pwed"
					Path p = Paths.get(fileName);
					if (Files.exists(p.getParent()) == false)
					{
						Files.createDirectories(p.getParent());
					}
					String jdbc = "jdbc:h2:" + p.toAbsolutePath();
					con = DriverManager.getConnection(jdbc, "sa", "");

				Statement stmt = con.createStatement();
					String sql2 = ("CREATE TABLE IF NOT EXISTS OBJ_STORE\n" +
							"(\n" +
							"	_INDEX			 					BIGINT  PRIMARY KEY,\n" +
							"	_OBJ_DATA			 				VARBINARY\n" +
							");\n" +
							" ");

					stmt.execute(sql2);
					stmt.close();

				}
				catch(Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return con;
	}

	Statement getStatement()
	{
		try
		{
			return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override public int size()
	{
		try(Statement stmt = getStatement())
		{
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM OBJ_STORE");
			rs.next();
			return ((Number)rs.getObject(1)).intValue();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override public boolean contains(Object o)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public Iterator<E> iterator()
	{
		try
		{
			Statement stmt = getStatement();
			ResultSet rs = stmt.executeQuery("SELECT _INDEX, _OBJ_DATA from OBJ_STORE ORDER BY _INDEX");

			return new Iterator<E>()
			{
				@Override public boolean hasNext()
				{
					try
					{
						if(rs.isBeforeFirst() == true) return true;
						boolean hasNext = rs.next();
						rs.previous();
						return hasNext;
					}
					catch(Exception e)
					{
						throw new RuntimeException(e);
					}
				}

				@Override public E next()
				{
					try
					{
						rs.next();
						byte[] data = rs.getBytes(2);
						try (ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(data)))
						{
							return (E) ous.readObject();
						}
					}
					catch(Exception e)
					{
						throw new RuntimeException(e);
					}
				}
			};

		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override public Object[] toArray()
	{
		Object[] items = new Object[size()];
		Iterator<E> iter = iterator();
		int index = 0;
		while(iter.hasNext())
		{
			items[index] = iter.next();
			index++;
		}
		return items;
	}

	@Override public <T> T[] toArray(T[] a)
	{
		Iterator<E> iter = iterator();
		for (int i = 0; i < a.length ; i++)
		{
			while(iter.hasNext())
			{
				a[i] = (T)iter.next();
			}
		}
		return a;
	}

	@Override public boolean add(E e)
	{
		Connection myCon = getConnection();
		try(PreparedStatement prstmt = myCon.prepareStatement("INSERT INTO OBJ_STORE VALUES (?,?)"))
		{
			byte[] data = null;
			try(ByteArrayOutputStream bous = new ByteArrayOutputStream();
	  			ObjectOutputStream ous = new ObjectOutputStream(bous))
			{
				ous.writeObject(e);
				ous.flush();
				bous.flush();
				data = bous.toByteArray();
			}

			int nextIndex = size();
			prstmt.setLong(1, nextIndex);
			prstmt.setBytes(2, data);
			prstmt.executeUpdate();
			return true;
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@Override public boolean remove(Object o)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public boolean containsAll(Collection<?> c)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public boolean addAll(Collection<? extends E> c)
	{
		for(E e : c)
		{
			add(e);
		}
		return true;
	}

	@Override public boolean addAll(int index, Collection<? extends E> c)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public boolean removeAll(Collection<?> c)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public boolean retainAll(Collection<?> c)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public void clear()
	{
		try(Statement stmt = getStatement())
		{
			stmt.execute("TRUNCATE TABLE OBJ_STORE");
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override public E get(int index)
	{
		try(Statement stmt = getStatement())
		{
			ResultSet rs = stmt.executeQuery("SELECT _OBJ_DATA from OBJ_STORE where _INDEX = " + index);
			if(rs.next())
			{
				byte[] data = rs.getBytes(1);
				try(ObjectInputStream ous = new ObjectInputStream(new ByteArrayInputStream(data)))
				{
					return (E)ous.readObject();
				}
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override public E set(int index, E element)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public void add(int index, E element)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public E remove(int index)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public int indexOf(Object o)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public int lastIndexOf(Object o)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public ListIterator<E> listIterator()
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public ListIterator<E> listIterator(int index)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public List<E> subList(int fromIndex, int toIndex)
	{
		throw new RuntimeException("Not implemented !");
	}

	@Override public void close() throws IOException
	{
		try
		{
			con.close();
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
	}
}
