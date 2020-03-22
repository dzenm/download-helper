package com.dzenm.lib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dzenm
 * @date 2019-08-23 15:53
 */

@Retention(RetentionPolicy.SOURCE)
public @interface Method {

    String METHOD_GET = "GET";

    String METHOD_POST = "POST";

    String METHOD_PUT = "PUT";

    String METHOD_DELETE = "DELETE";
}
