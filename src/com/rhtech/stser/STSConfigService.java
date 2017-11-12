package com.rhtech.stser;

import java.util.regex.Pattern;

public class STSConfigService {

    final Pattern configPattern = Pattern.compile("sts*(\\([0-9]+\\)).txt");
    final String STS_CONFIG_DIR = "STS";

    public STSConfigService() {
    }


}
