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
package raptor.speech;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.service.ThreadService;

public class ProcessSpeech implements Speech {
	protected Queue<String> speakQueue;
	private static final long PROCESS_SPEECH_MAX_TIME = 15000;

	protected String command;

	public ProcessSpeech(String command) {
		this.command = command;
		speakQueue = new ConcurrentLinkedQueue<String>();
	}

	public void dispose() {
	}

	public void init() {
	}

	public void speak(final String text) {
		if (StringUtils.isBlank(text))
			return;

		speakQueue.add(text);
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				synchronized (ProcessSpeech.this) {
					try {
						long startTime = System.currentTimeMillis();
						Process process = Runtime.getRuntime().exec(new String[] { command, speakQueue.poll() });

						while (System.currentTimeMillis() - startTime < PROCESS_SPEECH_MAX_TIME) {
							try {
								process.exitValue();
								break;
							} catch (IllegalThreadStateException ie) {
								try {
									Thread.sleep(250);
								} catch (InterruptedException ie2) {									
								}
							}
						}

						// if process is still alive destroy it.
						try {
							process.exitValue();
						} catch (IllegalThreadStateException ie) {
							try {
								process.destroy();
							} catch (Throwable t) {
							}
						}
					} catch (Exception e) {
						Raptor.getInstance().onError("Error occured speaking text: " + text, e);
					}
				}
			}
		});
	}
}
