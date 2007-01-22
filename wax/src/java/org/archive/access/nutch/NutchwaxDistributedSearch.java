/* Nutchwax
 * 
 * $Id$
 * 
 * Created on November 14th, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 * 
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 * 
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 * 
 * Heritrix is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.access.nutch;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.ipc.RPC;
import org.apache.nutch.searcher.NutchBean;

/** 
 * Script to start up a Nutchwax Distributed Searcher.
 * @author stack
 * @version $Revision$ $Date$
 */
public class NutchwaxDistributedSearch {
	public static class Server {
		private Server() {
			super();
		}

		/** 
		 * Use to start org.apache.nutch.searcher.DistributedSearch$Server
		 * but with nutchwax configuration mixed in so nutchwax plugins
		 * can be found (and properly configured).
		 */
		public static void main(String[] args) throws Exception {
			String usage =
				"NutchwaxDistributedSearch$Server <port> <index dir>";

			if (args.length == 0 || args.length > 2) {
				System.err.println(usage);
				System.exit(-1);
			}

			int port = Integer.parseInt(args[0]);
			Path directory = new Path(args[1]);

			Configuration conf = NutchwaxConfiguration.getConfiguration();
			NutchBean bean = new NutchBean(conf, directory);

			org.apache.hadoop.ipc.Server server = RPC.getServer(bean,
					"0.0.0.0", port, 10, true, conf);
			server.start();
			server.join();
		}
	}
}