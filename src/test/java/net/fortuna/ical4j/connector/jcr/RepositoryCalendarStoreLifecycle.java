/**
 * Copyright (c) 2008, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.connector.jcr;

import java.io.File;
import java.util.Hashtable;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.CalendarStoreLifecycle;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.jndi.RegistryHelper;

/**
 * $Id$
 *
 * Created on 01/03/2008
 *
 * @author Ben
 */
public class RepositoryCalendarStoreLifecycle implements CalendarStoreLifecycle {

    private static final String BASE_TEST_DIR = System
            .getProperty("java.io.tmpdir")
            + File.separator + "iCal4j_Connector_test" + File.separator;

    private String name;

    private Context context;
    
    private String repoName;

    private Repository repository;

    private CalendarStore store;

    /**
     * @param id
     */
    public RepositoryCalendarStoreLifecycle(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStoreLifecycle#getCalendarStore()
     */
    public CalendarStore getCalendarStore() {
        return store;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStoreLifecycle#shutdown()
     */
    public void shutdown() throws Exception {
        RegistryHelper.unregisterRepository(context, repoName);
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStoreLifecycle#startup()
     */
    public void startup() throws Exception {
        // bind repository..
        Hashtable env = new Hashtable();
        // env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "localhost");
        context = new InitialContext(env);

        repoName = name; // "Test Calendar Repository";

        File testDir = new File(BASE_TEST_DIR, repoName);
        FileUtils.deleteDirectory(testDir);
        RegistryHelper.registerRepository(context, repoName,
                "src/test/resources/repository.xml", testDir.getAbsolutePath(), false);

        repository = (Repository) context.lookup(repoName);
        // repository = new TransientRepository("test/repository.xml", testDir.getAbsolutePath());
        store = new RepositoryCalendarStore(repository);
    }

}
