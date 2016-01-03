/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2016 RaptorProject (https://github.com/Raptor-Fics-Interface/Raptor)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.service;

import java.io.File;

import raptor.engine.uci.UCIEngine;
import raptor.util.OSUtils;
import raptor.util.RaptorLogger;

public class UCIEngineService {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(UCIEngineService.class);
	public static boolean serviceCreated = false;
	public static UCIEngineService singletonInstance;

	public static UCIEngineService getInstance() {
		if (singletonInstance != null)
			return singletonInstance;

		singletonInstance = new UCIEngineService();
		return singletonInstance;
	}

	protected UCIEngine engine;

	private UCIEngineService() {
	}

	public void dispose() {
		if (engine != null)
			engine.quit();
	}

	public UCIEngine getEngine() {
		if (engine == null)
			synchronized (this) {
				if (engine == null) {

					UCIEngine engine = new UCIEngine();
					engine.setDefault(true);
					engine.setUserName("Stockfish 6");
					engine.setSupportsFischerRandom(true);
					engine.setMultiplyBlackScoreByMinus1(true);
					engine.setProcessPath(getProcessorPath());
					return engine;
				}
			}
		return engine;
	}

	private String getProcessorPath() {
		String result = null;

		boolean isDevMode = !new File("./stockfish").exists();

		if (isDevMode) {
			if (OSUtils.isLikelyOSX()) {
				result = "./projectFiles/Raptor.app.cocoa64/contents/MacOS/stockfish/Mac/stockfish-6-64";
			} else if (OSUtils.isLikelyLinux()) {
				result = "./projectFiles/linux/stockfish/Linux/stockfish_6_x64";
			} else if (OSUtils.isLikelyWindows()) {
				result = "./projectFiles/Windows/stockfish/Windows/stockfish-6-64.exe";
			}
		} else {
			if (OSUtils.isLikelyOSX()) {
				result = "stockfish/Mac/stockfish-6-64";
			} else if (OSUtils.isLikelyLinux()) {
				result = "stockfish/Linux/stockfish_6_x64";
			} else if (OSUtils.isLikelyWindows()) {
				result = "stockfish/Windows/stockfish-6-64.exe";
			}
		}

		File file = new File(result);
		if (file.exists()) {
			return file.getAbsolutePath();
		} else {
			throw new IllegalStateException(
					"Can not determine path to stockfish. This is invalid: "
							+ result);
		}
	}
}
