/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.hadoop.integration;

import java.io.File;

import org.apache.commons.logging.LogFactory;
import org.elasticsearch.hadoop.util.StringUtils;
import org.elasticsearch.hadoop.util.TestUtils;
import org.junit.rules.ExternalResource;

public class LocalEs extends ExternalResource {

    private static EsEmbeddedServer master;
    private static EsEmbeddedServer slave;

    public static final String CLUSTER_NAME = "ES-HADOOP-TEST";
    private static final String ES_DATA_PATH = "build/es.data";
    public static final String DATA_PORTS = "9500-9599";
    public static final String TRANSPORT_PORTS = "9600-9699";
    public static final String DATA_PORTS_SLAVE = "9700-9799";
    public static final String TRANSPORT_PORTS_SLAVE = "9800-9899";

    private boolean USE_SLAVE = false;
    private boolean disabled = false;

    @Override
    protected void before() throws Throwable {

        String host = HdpBootstrap.hadoopConfig().get("es.host");
        String port = HdpBootstrap.hadoopConfig().get("es.port");
        if (StringUtils.hasText(host)) {
            disabled = true;
            LogFactory.getLog(getClass()).warn("es.host specified; assuming an external instance and bailing out...");
            return;
        }

        if (master == null) {
            System.out.println("Starting Elasticsearch Master...");
            master = new EsEmbeddedServer(CLUSTER_NAME, ES_DATA_PATH, DATA_PORTS, TRANSPORT_PORTS);
            master.start();
        }

        if (USE_SLAVE && slave == null) {
            System.out.println("Starting Elasticsearch Slave...");
            slave = new EsEmbeddedServer(CLUSTER_NAME, ES_DATA_PATH, DATA_PORTS, TRANSPORT_PORTS);
            slave.start();
        }
    }

    @Override
    protected void after() {
        if (master != null) {
            if (USE_SLAVE && slave != null) {
                System.out.println("Stopping Elasticsearch Slave...");
                slave.stop();
                slave = null;
            }

            System.out.println("Stopping Elasticsearch Master...");
            try {
                master.stop();
            } catch (Exception ex) {
                // ignore
            }
            master = null;

            // delete data folder
            TestUtils.delete(new File(ES_DATA_PATH));
        }
    }
}
