package com.github.open.courier.core.support;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * new CourierVersion("1.5.5").lessThan("1.5.6")<br/>
 * new CourierVersion("1.5.5-SNAPSHOT").lessThan("1.5.6")<br/>
 * new CourierVersion("1.5.6-SNAPSHOT").equal("1.5.6")<br/>
 * new CourierVersion("1.5.6").equal("1.5.6")<br/>
 * new CourierVersion("1.5.7").greaterThan("1.5.6")<br/>
 * new CourierVersion("1.5.7-SNAPSHOT").greaterThan("1.5.6")
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 1.5.6 2022/8/12
 */
@AllArgsConstructor
public class CourierVersion {

    @Setter(AccessLevel.PRIVATE)
    private static String version;

    public CourierVersion(String version) {
        setVersion(version);
    }

    public boolean lessThan(String version) {
        return compareVersion(version, version) < 0;
    }

    public boolean equal(String version) {
        return compareVersion(version, version) == 0;
    }

    public boolean greaterThan(String version) {
        return compareVersion(version, version) > 0;
    }

    public static String get() {
        return version;
    }

    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] v1Array = version1.split("\\.");
        String[] v2Array = version2.split("\\.");

        for (int i = 0; i < Math.min(v1Array.length, v2Array.length); i++) {
            int v1 = parseInt(v1Array[i]);
            int v2 = parseInt(v2Array[i]);
            if (v1 != v2) {
                return v1 > v2 ? 1 : -1;
            }
        }
        //基础版本相同，再比较子版本号
        if (v1Array.length == v2Array.length) {
            //基础版本相同，无子版本号
            return 0;
        } else {
            return v1Array.length > v2Array.length ? 1 : -1;
        }
    }

    private static int parseInt(String subVersion) {
        if (subVersion.contains("-SNAPSHOT")) {
            return Integer.parseInt(subVersion.replace("-SNAPSHOT", ""));
        }
        return Integer.parseInt(subVersion);
    }

}
