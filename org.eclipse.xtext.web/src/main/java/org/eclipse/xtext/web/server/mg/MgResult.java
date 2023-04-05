/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import org.eclipse.xtext.web.server.IUnwrappableServiceResult;

public class MgResult implements IUnwrappableServiceResult {
    private final Object ret;

	@Override
    public int hashCode() {
        return ret.hashCode();
    }

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MgResult)) return false;
        MgResult mr = (MgResult) o;
        return ret.equals(mr.ret);
    }

    MgResult(Object ret) {
        this.ret = ret;
    }

	@Override
	public String getContent() {
		return ret.toString();
	}

	@Override
	public String getContentType() {
		return "text/html";
	}
}
