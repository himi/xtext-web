/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.web.server.persistence;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.web.server.IServiceContext;
import org.eclipse.xtext.web.server.model.IWebDocumentProvider;
import org.eclipse.xtext.web.server.model.IWebResourceSetProvider;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocument;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Resource handler that reads and writes files. The file paths are given by an
 * implementation of {@link IResourceBaseProvider}.
 */
public class FileResourceHandler implements IServerResourceHandler {
    private static Logger LOG = LoggerFactory.getLogger(FileResourceHandler.class);

	@Inject
	private IResourceBaseProvider resourceBaseProvider;

	@Inject
	private IWebResourceSetProvider resourceSetProvider;

	@Inject
	private IWebDocumentProvider documentProvider;

	@Inject
	private IEncodingProvider encodingProvider;

	@Override
	public XtextWebDocument get(String resourceId, IServiceContext serviceContext) throws IOException {
		try {
			URI uri = resourceBaseProvider.getFileURI(resourceId);
			if (uri == null) {
				throw new IOException("The requested resource does not exist.");
			}
			ResourceSet resourceSet = resourceSetProvider.get(resourceId, serviceContext);
			XtextResource resource = (XtextResource) resourceSet.getResource(uri, true);
			XtextWebDocument document = documentProvider.get(resourceId, serviceContext);
			document.setInput(resource);
			resourceSetProvider.updateIndex(document);
			return document;
		} catch (WrappedException exception) {
			throw Exceptions.sneakyThrow(exception.getCause());
		}
	}

	@Override
	public void put(IXtextWebDocument document, IServiceContext serviceContext) throws IOException {
		try {
			URI uri = resourceBaseProvider.getFileURI(document.getResourceId());
			XtextResource xres = document.getResource();
			try (OutputStreamWriter writer = new OutputStreamWriter(
					xres.getResourceSet().getURIConverter().createOutputStream(uri),
					encodingProvider.getEncoding(uri))) {
                LOG.info("Save to {}...", uri.toString());
				writer.write(document.getText());
                LOG.info("Done");
			} catch (WrappedException exception) {
				throw exception.getCause();
			}
		} catch (Throwable e) {
			throw Exceptions.sneakyThrow(e);
		}
	}
}
