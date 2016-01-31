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
package raptor.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import raptor.Raptor;
import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Partnership;
import raptor.connector.Connector;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.BughouseService.BughouseServiceListener;
import raptor.service.ThreadService;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chat.ChatUtils;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

public class BugTeams extends Composite {
	protected BughouseService service;
	protected Combo teamLowRating;
	protected Combo teamHighRating;
	protected Combo matchHighLowBoth;
	protected RaptorTable player1Table;
	protected RaptorTable player2Table;
	protected boolean isActive = false;
	protected Button autoMatch2_0;
	protected Button isRated;
	protected static L10n local = L10n.getInstance();

	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive && !isDisposed()) {
				service.refreshAvailablePartnerships();
				ThreadService.getInstance().scheduleOneShot(
						Raptor.getInstance().getPreferences().getInt(PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL)
								* 1000,
						this);
			}
		}
	};

	protected BughouseServiceListener listener = new BughouseServiceListener() {
		public void availablePartnershipsChanged(Partnership[] newPartnerships) {
			refreshTable();
		}

		public void gamesInProgressChanged(BugGame[] newGamesInProgress) {
		}

		public void unpartneredBuggersChanged(Bugger[] newUnpartneredBuggers) {
		}
	};

	public BugTeams(Composite parent, final BughouseService service) {
		super(parent, SWT.NONE);
		this.service = service;
		init();
		service.addBughouseServiceListener(listener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				isActive = false;
				service.removeBughouseServiceListener(listener);
			}
		});
	}

	public Connector getConnector() {
		return service.getConnector();
	}

	public void init() {
		setLayout(new GridLayout(1, false));

		Composite ratingFilterComposite = new Composite(BugTeams.this, SWT.NONE);
		ratingFilterComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		ratingFilterComposite.setLayout(new RowLayout());

		teamLowRating = new Combo(ratingFilterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		teamLowRating.add("0");
		teamLowRating.add("1000");
		teamLowRating.add("2000");
		teamLowRating.add("2500");
		teamLowRating.add("3000");
		teamLowRating.add("3200");
		teamLowRating.add("3400");
		teamLowRating.add("3600");
		teamLowRating.add("3800");
		teamLowRating.add("4000");
		teamLowRating.add("4200");
		teamLowRating.add("4400");
		teamLowRating.add("4600");
		teamLowRating.add("4800");
		teamLowRating.add("5000");
		teamLowRating.add("5200");
		teamLowRating.add("5400");
		teamLowRating.add("5600");
		teamLowRating.add("5800");
		teamLowRating.add("6000");
		teamLowRating.add("6200");
		teamLowRating.add("6400");
		teamLowRating.add("9999");

		teamLowRating.select(Raptor.getInstance().getPreferences().getInt(PreferenceKeys.BUG_ARENA_TEAMS_LOW_INDEX));
		teamLowRating.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(PreferenceKeys.BUG_ARENA_TEAMS_LOW_INDEX,
						teamLowRating.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		CLabel label2 = new CLabel(ratingFilterComposite, SWT.LEFT);
		label2.setText(local.getString("bugTeams5"));

		teamHighRating = new Combo(ratingFilterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		teamHighRating.add("0");
		teamHighRating.add("1000");
		teamHighRating.add("2000");
		teamHighRating.add("2500");
		teamHighRating.add("3000");
		teamHighRating.add("3200");
		teamHighRating.add("3400");
		teamHighRating.add("3600");
		teamHighRating.add("3800");
		teamHighRating.add("4000");
		teamHighRating.add("4200");
		teamHighRating.add("4400");
		teamHighRating.add("4600");
		teamHighRating.add("4800");
		teamHighRating.add("5000");
		teamHighRating.add("5200");
		teamHighRating.add("5400");
		teamHighRating.add("5600");
		teamHighRating.add("5800");
		teamHighRating.add("6000");
		teamHighRating.add("6200");
		teamHighRating.add("6400");
		teamHighRating.add("9999");

		teamHighRating.select(Raptor.getInstance().getPreferences().getInt(PreferenceKeys.BUG_ARENA_TEAMS_HIGH_INDEX));
		teamHighRating.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(PreferenceKeys.BUG_ARENA_TEAMS_HIGH_INDEX,
						teamHighRating.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		player1Table = new RaptorTable(tableComposite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player1Table.addColumn(local.getString("bugTeams.teamElo"), SWT.LEFT, 20, false, null);
		player1Table.addColumn(local.getString("bugTeams.name"), SWT.LEFT, 50, false, null);
		player1Table.addColumn(local.getString("bugTeams.status"), SWT.LEFT, 30, false, null);
		player1Table.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(BugTeams.this.getShell(), SWT.POP_UP);
				ChatUtils.addPersonMenuItems(menu, getConnector(), parsePlayerName(rowData[1]));
				if (menu.getItemCount() > 0) {
					menu.setLocation(player1Table.getTable().toDisplay(event.x, event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!BugTeams.this.getDisplay().readAndDispatch()) {
							BugTeams.this.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}
		});

		player2Table = new RaptorTable(tableComposite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player2Table.addColumn(local.getString("bugTeams.name"), SWT.LEFT, 50, false, null);
		player2Table.addColumn(local.getString("bugTeams.status"), SWT.LEFT, 30, false, null);
		player2Table.addRaptorTableListener(new RaptorTableAdapter() {

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(BugTeams.this.getShell(), SWT.POP_UP);
				ChatUtils.addPersonMenuItems(menu, getConnector(), parsePlayerName(rowData[0]));
				if (menu.getItemCount() > 0) {
					menu.setLocation(player2Table.getTable().toDisplay(event.x, event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!BugTeams.this.getDisplay().readAndDispatch()) {
							BugTeams.this.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}

		});

		isRated = new Button(BugTeams.this, SWT.CHECK);
		isRated.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
		isRated.setText(local.getString("bugTeams4"));
		isRated.setSelection(Raptor.getInstance().getPreferences().getBoolean(PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED));
		isRated.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED,
						isRated.getSelection());
			}
		});

		autoMatch2_0 = new Button(BugTeams.this, SWT.CHECK);
		autoMatch2_0.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
		autoMatch2_0.setText(local.getString("bugTeamsAutoMatch20"));
		autoMatch2_0.setSelection(false);
		autoMatch2_0.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (autoMatch2_0.getSelection()) {
					matchAll(2, 0);
				}
			}
		});

		Composite controlsComposite = new Composite(BugTeams.this, SWT.NONE);
		controlsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		controlsComposite.setLayout(new GridLayout(4, false));

		CLabel label = new CLabel(controlsComposite, SWT.LEFT);
		label.setText(local.getString("bugTeams6"));

		matchHighLowBoth = new Combo(controlsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		matchHighLowBoth.add(local.getString("bugTeams7"));
		matchHighLowBoth.add(local.getString("bugTeams8"));
		matchHighLowBoth.add(local.getString("bugTeams9"));
		matchHighLowBoth.select(Raptor.getInstance().getPreferences().getInt(PreferenceKeys.BUG_ARENA_HI_LOW_INDEX));
		matchHighLowBoth.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(PreferenceKeys.BUG_ARENA_HI_LOW_INDEX,
						matchHighLowBoth.getSelectionIndex());
			}
		});

		Composite matchComposite = new Composite(BugTeams.this, SWT.NONE);
		matchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		matchComposite.setLayout(new GridLayout(4, false));

		CLabel matchSelectedLabel = new CLabel(matchComposite, SWT.LEFT);
		matchSelectedLabel.setText(local.getString("bugTeams10"));

		Button selected10Button = new Button(matchComposite, SWT.PUSH);
		selected10Button.setText("1 0");
		selected10Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(true, selectedIndexes[i], 1, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 1, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
				}
			}
		});

		Button selected20Button = new Button(matchComposite, SWT.PUSH);
		selected20Button.setText("2 0");
		selected20Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(true, selectedIndexes[i], 2, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 2, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
				}
			}
		});

		Button selected30Button = new Button(matchComposite, SWT.PUSH);
		selected30Button.setText("3 0");
		selected30Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(true, selectedIndexes[i], 3, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable().getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 3, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
				}
			}
		});

		CLabel matchAllLabel = new CLabel(matchComposite, SWT.LEFT);
		matchAllLabel.setText(local.getString("bugTeams12"));

		Button all10Button = new Button(matchComposite, SWT.PUSH);
		all10Button.setText("1 0");
		all10Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					matchAll(1, 0);
				}
			}
		});

		Button all20Button = new Button(matchComposite, SWT.PUSH);
		all20Button.setText("2 0");
		all20Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					matchAll(2, 0);

				}
			}

		});

		Button all30Button = new Button(matchComposite, SWT.PUSH);
		all30Button.setText("3 0");
		all30Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				matchAll(3, 0);
			}
		});
		service.refreshAvailablePartnerships();
		refreshTable();
	}

	private void matchAll(int time, int inc) {
		synchronized (player1Table) {
			String matchHighLow = matchHighLowBoth.getText();
			boolean isUserHigh = isUserHigh();
			int items = player1Table.getTable().getItems().length;

			for (int i = 0; i < items; i++) {
				if (local.getString("bugTeams7").equals(matchHighLow)) {
					if ((isHigh(true, i) && isUserHigh) || (!isHigh(true, i) && !isUserHigh)) {
						match(true, i, time, inc);
					} else {
						match(false, i, time, inc);

					}
				} else if (local.getString("bugTeams8").equals(matchHighLow)) {
					if ((!isHigh(true, i) && isUserHigh) || (isHigh(true, i) && !isUserHigh)) {
						match(true, i, time, inc);
					} else {
						match(false, i, time, inc);

					}
				} else {
					match(true, i, time, inc);
					match(false, i, time, inc);
				}
			}
		}
	}

	private boolean isUserHigh() {
		int userRow = -1;
		boolean result = false;

		synchronized (player1Table) {
			int items = player1Table.getTable().getItems().length;
			for (int i = 0; userRow == -1 && i < items; i++) {
				if (getPlayerName(i, true).equalsIgnoreCase(service.getConnector().getUserName())) {
					userRow = i;
					result = isHigh(true, i);
				}
			}

			if (userRow == -1) {
				for (int i = 0; userRow == -1 && i < items; i++) {
					if (getPlayerName(i, false).equalsIgnoreCase(service.getConnector().getUserName())) {
						userRow = i;
						result = isHigh(false, i);
					}
				}
			}
		}
		return result;
	}

	private boolean isHigh(boolean isTable1, int row) {
		int player1EloInt = getPlayerRating(row, true);
		int player2EloInt = getPlayerRating(row, false);

		return isTable1 ? player1EloInt > player2EloInt : player2EloInt > player1EloInt;
	}

	private void match(boolean isTable1, int row, int time, int inc) {
		String player1Name = getPlayerName(row, true);
		String player2Name = getPlayerName(row, false);

		String userName = service.getConnector().getUserName();

		if (!userName.equalsIgnoreCase(player1Name) && !userName.equalsIgnoreCase(player2Name)) {
			service.getConnector().matchBughouse(isTable1 ? player1Name : player2Name, isRated.getSelection(), time,
					inc);
		}
	}

	private int getPlayerRating(int row, boolean isPlayer1Table) {
		String fullText = (isPlayer1Table ? player1Table : player2Table).getRowText(row)[isPlayer1Table ? 1 : 0];
		RaptorStringTokenizer tok = new RaptorStringTokenizer(fullText, " ");
		int result = 0;

		try {
			result = Integer.parseInt(tok.nextToken());
		} catch (NumberFormatException e) {
		}
		return result;
	}

	private String getPlayerName(int row, boolean isPlayer1Table) {
		String fullText = (isPlayer1Table ? player1Table : player2Table).getRowText(row)[isPlayer1Table ? 1 : 0];
		return parsePlayerName(fullText);
	}

	private String parsePlayerName(String playerAndRatingValue) {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(playerAndRatingValue, " ");
		tok.nextToken();
		return tok.nextToken();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshAvailablePartnerships();
			ThreadService.getInstance().scheduleOneShot(
					Raptor.getInstance().getPreferences().getInt(PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
					timer);
		}

	}

	public void onPassivate() {
		if (isActive) {
			isActive = false;
		}
	}

	protected Partnership[] getFilteredPartnerships() {
		Partnership[] current = service.getAvailablePartnerships();
		if (current == null) {
			current = new Partnership[0];
		}
		List<Partnership> result = new ArrayList<Partnership>(current.length);
		for (Partnership partnership : current) {
			if (passesFilterCriteria(partnership)) {
				result.add(partnership);
			}
		}
		Collections.sort(result);
		return result.toArray(new Partnership[0]);
	}

	protected boolean passesFilterCriteria(Partnership partnership) {
		int lowRating = Integer.parseInt(teamLowRating.getText());
		int highRating = Integer.parseInt(teamHighRating.getText());
		int teamRating = partnership.getTeamRating();
		return teamRating >= lowRating && teamRating <= highRating;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (player1Table.isDisposed()) {
					return;
				}

				synchronized (player1Table.getTable()) {

					Partnership[] partnerships = getFilteredPartnerships();
					String[][] player1Data = new String[partnerships.length][3];
					String[][] player2Data = new String[partnerships.length][3];

					for (int i = 0; i < partnerships.length; i++) {
						Partnership partnership = partnerships[i];
						player1Data[i][0] = "" + partnership.getTeamRating();
						player1Data[i][1] = partnership.getBugger1().getRating() + " "
								+ partnership.getBugger1().getName();
						player1Data[i][2] = partnership.getBugger1().getStatus().toString();

						player2Data[i][0] = partnership.getBugger2().getRating() + " "
								+ partnership.getBugger2().getName();
						player2Data[i][1] = partnership.getBugger2().getStatus().toString();

					}
					player1Table.refreshTable(player1Data);
					player2Table.refreshTable(player2Data);
				}

				if (autoMatch2_0 != null && connector != null && (autoMatch2_0.getSelection() && !connector.isLoggedInUserPlayingAGame())) {
					matchAll(2, 0);
				}
			}
		});
	}
}