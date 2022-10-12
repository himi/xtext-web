/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

import org.eclipse.xtext.web.server.IServiceResult;

public class OutlineResult implements IServiceResult {
    public final OutlineElement[] result;

	@Override
    public int hashCode() {
        int val = 0;
        for (OutlineElement oe: result) {
            val ^= oe.hashCode();
        }
        return val;
    }

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutlineResult)) return false;
        OutlineResult or = (OutlineResult) o;
        if (result.length != or.result.length) return false;
        for (int i = 0; i < result.length; i++) {
            if (!result[i].equals(or.result[i])) return false;
        }
        return true;
    }

    OutlineResult(OutlineElement[] result) {
        this.result = result;
    }

    static OutlineResult emptyResult() {
        return new OutlineResult(new OutlineElement[0]);
    }
}
