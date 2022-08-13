package uk.co.devworx;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

public class EchoServlet extends HttpServlet
{
	private void doProcess(String method, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("---------------------------------------------------------------------------------------------------------\n");
		bldr.append(method + "\n");
		bldr.append(req.getRequestURL() + "\n");
		bldr.append("---------------------------------------------------------------------------------------------------------\n");
		final Enumeration<String> headerNames = req.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			final String name = headerNames.nextElement();
			final Enumeration<String> headers = req.getHeaders(name);
			bldr.append(name + " : ");
			while(headers.hasMoreElements())
			{
				bldr.append("[" + headers.nextElement() + "] ");
			}
			bldr.append("\n");
		}
		bldr.append("---------------------------------------------------------------------------------------------------------\n");
		final ServletInputStream inputStream = req.getInputStream();
		bldr.append("Has Input Stream : " + inputStream + " \n");
		if(inputStream != null)
		{
			byte[] data = new byte[4096];
			int read = 0;
			int counter = 0;
			StringBuilder content = new StringBuilder();
			ByteArrayOutputStream bous = new ByteArrayOutputStream();
			while((read = inputStream.read(data)) != -1)
			{
				counter += read;
				bous.write(data, 0, read);
			}
			bldr.append("Byte Length : " + counter + "\n");
			bldr.append("Bytes : \n");
			bldr.append(new String(bous.toByteArray()));
		}
		bldr.append("---------------------------------------------------------------------------------------------------------\n");

		System.out.println(bldr);
	}

	@Override public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("GET",req, resp);
	}

	@Override public long getLastModified(HttpServletRequest req)
	{
		return super.getLastModified(req);
	}

	@Override public void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("HEAD",req, resp);
	}

	@Override public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("POST",req, resp);
	}

	@Override public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("PUT",req, resp);

	}

	@Override public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("DELETE" ,req, resp);

	}

	@Override public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("OPTIONS" ,req, resp);
	}

	@Override public void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doProcess("TRACE" ,req, resp);
	}
}
