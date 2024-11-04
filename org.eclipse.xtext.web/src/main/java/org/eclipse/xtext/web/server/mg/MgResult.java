/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import org.eclipse.xtext.web.server.IUnwrappableServiceResult;

import com.google.gson.JsonPrimitive;

public class MgResult implements IUnwrappableServiceResult {
    private final String json;

    public static final MgResult NONE = new MgResult("");

    private static String quote(String str) {
        //return str.replace("\r", "").replace("\n", "\\n").replace("\"", "\\\"");
        return new JsonPrimitive(str).toString();
    }

	@Override
    public int hashCode() {
        return json.hashCode();
    }

    public static MgResult error(String msg) {
        return new MgResult("{error: " + quote(msg) + "}");
    }

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MgResult)) return false;
        MgResult mr = (MgResult) o;
        return json.equals(mr.json);
    }

    private static String convert(String str) {
        return quote(str);
    }

    private static String convert(int num) {
        return Integer.toString(num);
    }

    private static String convertVal(Object obj) {
        if (obj instanceof Number) {
            return obj.toString();
        } else if (obj instanceof String) {
            return convert((String) obj);
        } else {
            return obj.toString();
        }
    }

    public static MgResult create(String str) {
        return new MgResult(convert(str));
    }

    public static MgResult num(int num) {
        return new MgResult(convert(num));
    }

    public static MgResult obj(Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < args.length;) {
            if (i > 0) {
                sb.append(',');
            }
            String key = (String) args[i++];
            Object val = args[i++];
            sb.append(convert(key));
            sb.append(':');
            sb.append(convertVal(val));
        }
        sb.append('}');

        return new MgResult(sb.toString());
    }

    /* package */ MgResult(String json) {
        this.json = json;
    }

	@Override
	public String getContent() {
        return json;
	}

	@Override
	public String getContentType() {
		return "text/x-json";
	}
}
