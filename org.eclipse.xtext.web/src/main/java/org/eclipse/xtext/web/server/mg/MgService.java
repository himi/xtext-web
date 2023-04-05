/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.web.server.IServiceContext;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MgService {
	@Inject
	private IMg iMg;

    public Map<String, String> getArg(IServiceContext context) {
        Map<String, String> ret = new HashMap<>();
        for (String key : context.getParameterKeys()) {
            String val = context.getParameter(key);
            if (val != null) {
                ret.put(key, val);
            }
        }
        return ret;
    }

	public MgResult getResult(XtextWebDocumentAccess document, Map<String, String> args) {
		return document.readOnly(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
			public MgResult exec(IXtextWebDocument it, CancelIndicator cancelIndicator) throws Exception {
                Object ret = iMg.invoke(args);
				return new MgResult(ret);
			}
		});
	}
}
