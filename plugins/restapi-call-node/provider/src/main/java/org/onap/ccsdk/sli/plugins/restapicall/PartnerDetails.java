package org.onap.ccsdk.sli.plugins.restapicall;

public class PartnerDetails {
    protected String username;
    protected String password;
    protected String url;

    public PartnerDetails(String username, String password, String url) {
	this.username = username;
	this.password = password;
	this.url = url;
    }

}
