package com.architexa.extensions.entJava;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CachedEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		try {
			int cacheNameNdx = systemId.lastIndexOf("/");
			if (cacheNameNdx != -1) {
				String cacheName = systemId.substring(cacheNameNdx+1);
				InputStream cacheStream = Activator.getDefault().getInputStream("cached-external-entities/" + cacheName);
				if (cacheStream != null)
					return new InputSource(cacheStream);
			}
		} catch (Throwable t) {}
		
		// we couldn't find the file in cache - let the parser find it
		System.err.println("cached fail - systemId: " + systemId);
		return null;
	}

}
