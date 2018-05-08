package org.onap.ccsdk.sli.core.dblib;

import java.lang.reflect.Method;

/**
 * https://karaf.apache.org/manual/latest-2.x/developers-guide/extending-console.html
 * https://github.com/apache/karaf/tree/master/shell/console/src/main/java/org/apache/felix/gogo/commands
 */
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "dblib", name = "encode", description="Says hello")
public class EncShellCommand extends OsgiCommandSupport {
    private static Logger LOGGER = LoggerFactory.getLogger(EncShellCommand.class);

    @Argument(index = 0, name = "arg", description = "The command argument", required = true, multiValued = false)
    String arg = null;

    @Override
    protected Object doExecute() throws Exception {
        System.out.println(String.format("Original value: %s", arg));
        System.out.println(String.format("Encrypted value: %s", encrypt(arg)));
        return null;
    }

    private String encrypt(String value) {
        try {
            BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

            ServiceReference sref = bctx.getServiceReference("org.opendaylight.aaa.encrypt.AAAEncryptionService");
            Object encrSvc = bctx.getService(sref);

            Method gs2Method = encrSvc.getClass().getMethod("encrypt", new Class[] { "".getClass() });
            Object unmasked = gs2Method.invoke(encrSvc, new Object[] { value });
            return String.format("ENC:%s", unmasked.toString());

        } catch (Exception exc) {
            LOGGER.error("Failure", exc);
            return value;
        }
    }
}