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
package raptor.swt.chess.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.engine.uci.UCIBestMove;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.DepthInfo;
import raptor.engine.uci.info.NodesSearchedInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.engine.uci.info.TimeInfo;
import raptor.engine.uci.options.UCICheck;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.RaptorTable;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.EngineAnalysisWidget;
import raptor.util.RaptorLogger;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringUtils;

public class UciAnalysisWidget implements EngineAnalysisWidget {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(UciAnalysisWidget.class);

	protected ChessBoardController controller;
	protected Composite composite, topLine;
	protected UCIEngine engine;
	protected Combo engineCombo;
	protected RaptorTable bestMoves;
	protected Button startStopButton;
	protected static L10n local = L10n.getInstance();
	protected UCIInfoListener listener = new UCIInfoListener() {
		public void engineSentBestMove(UCIBestMove uciBestMove) {
		}

		public void engineSentInfo(final UCIInfo[] infos) {
			Raptor.getInstance().getDisplay()
					.asyncExec(new RaptorRunnable(controller.getConnector()) {
						@Override
						public void execute() {
							String score = null;
							String time = null;
							String depth = null;
							String nodes = null;
							List<String> pvs = new ArrayList<String>(3);

							for (UCIInfo info : infos) {
								if (info instanceof ScoreInfo) {
									ScoreInfo scoreInfo = (ScoreInfo) info;
									if (((ScoreInfo) info).getMateInMoves() != 0) {
										score = "Mate in "
												+ scoreInfo.getMateInMoves();
									} else if (scoreInfo.isLowerBoundScore()) {
										score = "-inf";
									} else if (scoreInfo.isUpperBoundScore()) {
										score = "+inf";
									} else {
										double scoreAsDouble = controller
												.getGame().isWhitesMove()
												|| !engine
														.isMultiplyBlackScoreByMinus1() ? scoreInfo
												.getValueInCentipawns() / 100.0
												: -scoreInfo
														.getValueInCentipawns() / 100.0;

										score = ""
												+ new BigDecimal(scoreAsDouble)
														.setScale(
																2,
																BigDecimal.ROUND_HALF_UP)
														.toString();
									}
								} else if (info instanceof DepthInfo) {
									DepthInfo depthInfo = (DepthInfo) info;
									depth = depthInfo.getSearchDepthPlies()
											+ " plies";
								} else if (info instanceof NodesSearchedInfo) {
									NodesSearchedInfo nodesSearchedInfo = (NodesSearchedInfo) info;
									nodes = RaptorStringUtils.formatAsNumber(""
											+ nodesSearchedInfo
													.getNodesSearched() / 1000);
								} else if (info instanceof TimeInfo) {
									TimeInfo timeInfo = (TimeInfo) info;
									time = new BigDecimal(timeInfo
											.getTimeMillis() / 1000.0)
											.setScale(1,
													BigDecimal.ROUND_HALF_UP)
											.toString();
								} else if (info instanceof BestLineFoundInfo) {
									BestLineFoundInfo bestLineFoundInfo = (BestLineFoundInfo) info;
									StringBuilder line = new StringBuilder(100);
									Game gameClone = controller.getGame()
											.deepCopy(true);
									gameClone.addState(Game.UPDATING_SAN_STATE);
									gameClone
											.clearState(Game.UPDATING_ECO_HEADERS_STATE);

									boolean isFirstMove = true;

									for (UCIMove move : bestLineFoundInfo
											.getMoves()) {
										try {
											Move gameMove = null;

											if (move.isPromotion()) {
												gameMove = gameClone.makeMove(
														move.getStartSquare(),
														move.getEndSquare(),
														move.getPromotedPiece());
											} else {
												gameMove = gameClone.makeMove(
														move.getStartSquare(),
														move.getEndSquare());
											}

											String san = GameUtils.convertSanToUseUnicode(
													gameMove.getSan(),
													gameMove.isWhitesMove());
											String moveNumber = isFirstMove
													&& !gameMove.isWhitesMove() ? gameMove
													.getFullMoveCount()
													+ ") ... " : gameMove
													.isWhitesMove() ? gameMove
													.getFullMoveCount() + ") "
													: "";
											line.append((line.equals("") ? ""
													: " ")
													+ moveNumber
													+ san
													+ (gameClone.isInCheck() ? "+"
															: "")
													+ (gameClone.isCheckmate() ? "#"
															: ""));
											isFirstMove = false;
										} catch (Throwable t) {
											if (LOG.isInfoEnabled()) {
												LOG.info(
														"Illegal line found skipping line (This can occur if the position was "
																+ "changing when the analysis line was being calculated).",
														t);
											}
											break;
										}
									}
									pvs.add(line.toString());
								}
							}

							if (score != null
									&& (!score.startsWith("+inf") && !score
											.startsWith("-inf"))) {
								final String finalScore = score;
								final String finalTime = time;
								final String finalDepth = depth;
								final String finalNodes = nodes;
								final List<String> finalPVs = pvs;

								Raptor.getInstance()
										.getDisplay()
										.asyncExec(
												new RaptorRunnable(controller
														.getConnector()) {
													@Override
													public void execute() {
														if (composite
																.isDisposed()) {
															return;
														}
														if (!finalPVs.isEmpty()) {
															String[][] data = new String[bestMoves
																	.getRowCount()
																	+ finalPVs
																			.size()][5];

															for (int i = 0; i < finalPVs
																	.size(); i++) {
																data[0][0] = StringUtils
																		.defaultString(finalScore);
																data[0][1] = StringUtils
																		.defaultString(finalDepth);
																data[0][2] = StringUtils
																		.defaultString(finalTime);
																data[0][3] = StringUtils
																		.defaultString(finalNodes);
																data[0][4] = StringUtils
																		.defaultString(finalPVs
																				.get(i));
															}

															for (int i = 0; i < bestMoves
																	.getRowCount(); i++) {
																for (int j = 0; j < bestMoves
																		.getColumnCount(); j++) {
																	data[i
																			+ finalPVs
																					.size()][j] = bestMoves
																			.getText(
																					i,
																					j);
																}
															}

															bestMoves
																	.refreshTable(data);
														} else if (bestMoves
																.getRowCount() > 0) {
															if (StringUtils
																	.isNotBlank(finalScore)) {
																bestMoves
																		.setText(
																				0,
																				0,
																				finalScore);
															}
															if (StringUtils
																	.isNotBlank(finalDepth)) {
																bestMoves
																		.setText(
																				0,
																				1,
																				finalDepth);
															}
															if (StringUtils
																	.isNotBlank(finalTime)) {
																bestMoves
																		.setText(
																				0,
																				2,
																				finalTime);
															}

															if (StringUtils
																	.isNotBlank(finalNodes)) {
																bestMoves
																		.setText(
																				0,
																				3,
																				finalNodes);
															}
														}
													}
												});
							}
						}
					});
		}
	};

	public void clear() {
		Raptor.getInstance().getDisplay()
				.asyncExec(new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						bestMoves.clearTable();
					}
				});
	}

	public Composite create(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (engine != null) {
					engine.quit();
				}
			}
		});

		topLine = new Composite(composite, SWT.LEFT);
		topLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginLeft = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginHeight = 2;
		rowLayout.marginWidth = 2;
		rowLayout.spacing = 0;
		topLine.setLayout(rowLayout);
		//
		// ?????
		// topLine.setLayout(new RowLayout());

		engineCombo = new Combo(topLine, SWT.DROP_DOWN | SWT.READ_ONLY);
		engineCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});

		startStopButton = new Button(topLine, SWT.FLAT);
		startStopButton.setText(local.getString("uciAnalW_31"));
		startStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (startStopButton.getText().equals(
						local.getString("uciAnalW_32"))) {
					start();
					startStopButton.setText(local.getString("uciAnalW_33"));
				} else {
					stop();
					startStopButton.setText(local.getString("uciAnalW_34"));
				}
			}
		});

		bestMoves = new RaptorTable(composite, SWT.BORDER | SWT.FULL_SELECTION,
				false, true);
		bestMoves.setToolTipText(local.getString("uciAnalW_37"));
		bestMoves.addColumn(local.getString("uciAnalW_38"), SWT.LEFT, 10,
				false, null);
		bestMoves.addColumn(local.getString("uciAnalW_39"), SWT.LEFT, 10,
				false, null);
		bestMoves.addColumn(local.getString("uciAnalW_40"), SWT.LEFT, 10,
				false, null);
		bestMoves.addColumn(local.getString("uciAnalW_41"), SWT.LEFT, 10,
				false, null);
		bestMoves.addColumn(local.getString("uciAnalW_42"), SWT.LEFT, 60,
				false, null);
		bestMoves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		bestMoves.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, final String[] rowData) {
				Menu menu = new Menu(UciAnalysisWidget.this.composite
						.getShell(), SWT.POP_UP);
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(local.getString("uciAnalW_43"));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Clipboard clipboard = new Clipboard(composite
								.getDisplay());
						String text = GameUtils.removeUnicodePieces(rowData[4]);
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] { textTransfer };
						Object[] data = new Object[] { text };
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				});
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!composite.getDisplay().readAndDispatch()) {
						composite.getDisplay().sleep();
					}
				}
				menu.dispose();
			}
		});

		updateEnginesCombo();

		updateFromPrefs();
		return composite;
	}

	public ChessBoardController getChessBoardController() {
		return controller;
	}

	public Composite getControl() {
		return composite;
	}

	public void onShow() {
		clear();
		updateEnginesCombo();
		// updateCustomButtons();
		start();
		composite.layout(true, true);
	}

	public void quit() {
		Raptor.getInstance().getDisplay()
				.asyncExec(new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						if (!composite.isDisposed()) {
							clear();
						}
					}
				});
		if (engine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					engine.quit();
				}
			});
		}
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void stop() {
		if (engine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					engine.quit();
					Raptor.getInstance().getDisplay()
							.asyncExec(new RaptorRunnable() {
								@Override
								public void execute() {
									startStopButton.setText(local
											.getString("uciAnalW_44"));
								}
							});
				}
			});
		}
	}

	public void updateFromPrefs() {
		Color background = Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_BACKGROUND_COLOR);
		composite.setBackground(background);
		topLine.setBackground(background);
	}

	public void updateToGame() {
		if (startStopButton.getText().equals(local.getString("uciAnalW_45"))) {
			start();
		}
	}

	public void start() {
		if (composite.isVisible()) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					if (LOG.isDebugEnabled()) {
						LOG.debug("In UciAnalysisWidget.start("
								+ engine.getUserName() + ")");
					}
					try {
						if (!engine.isConnected()) {
							engine.connect();
						}

						if (controller.getGame().getVariant() == Variant.fischerRandom
								&& engine.hasOption("UCI_Chess960")) {
							UCICheck opt = (UCICheck) engine
									.getOption("UCI_Chess960");
							opt.setValue("true");
						}

						engine.newGame();
						engine.setPosition(controller.getGame().toFen(), null);
						engine.isReady();
						engine.go(engine.getGoAnalysisParameters(), listener);
						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										startStopButton.setText(local
												.getString("uciAnalW_54"));
									}
								});

					} catch (Throwable t) {
						LOG.error("Error starting engine", t);
					} finally {
					}
				}
			});
		}
	}

	protected void updateEnginesCombo() {

		engineCombo.removeAll();

		UCIEngine[] engines = new UCIEngine[] { UCIEngineService.getInstance()
				.getEngine() };

		for (UCIEngine engine : engines)
			engineCombo.add(engine.getUserName());

		engineCombo.select(0);

		topLine.pack(true);
		topLine.layout(true, true);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Selecting UCIEngine " + engines[0].getUserName() + ")");
		}

		startStopButton.setText(local.getString("uciAnalW_7"));
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				try {
					engine = UCIEngineService.getInstance().getEngine()
							.getDeepCopy();
					if (LOG.isDebugEnabled()) {
						LOG.debug("Changing engine to : "
								+ engine.getUserName());
					}
					start();
				} catch (Throwable t) {
					LOG.error("Error switching chess engines", t);
				}
			}
		});
	}
}