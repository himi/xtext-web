/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.viz;

import org.eclipse.xtext.web.server.IUnwrappableServiceResult;

public class VizResult implements IUnwrappableServiceResult {
    private final String svg;

	@Override
    public int hashCode() {
        return svg.hashCode();
    }

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VizResult)) return false;
        VizResult vr = (VizResult) o;
        return svg.equals(vr.svg);
    }

    VizResult(String svg) {
        this.svg = svg;
    }

	@Override
	public String getContent() {
		return svg;
	}

	@Override
	public String getContentType() {
		return "image/svg+xml";
	}
}
