package raptor.pref.page;

import java.io.File;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;
import raptor.service.SoundService;

public class SoundsPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	protected static L10n local = L10n.getInstance();

	public SoundsPage() {
		super(FLAT);
		setTitle(local.getString("sounds"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		LabelFieldEditor userHomeDir = new LabelFieldEditor("NONE",
				WordUtils.wrap(local.getString("soundsText"), 70) + new File(Raptor.RESOURCES_DIR).getAbsolutePath(),
				getFieldEditorParent());
		addField(userHomeDir);

		for (final String soundKey : SoundService.getInstance().getSoundKeys()) {
			labelButtonFieldEditor = new LabelButtonFieldEditor("NONE", soundKey, getFieldEditorParent(), "Play",
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							SoundService.getInstance().playSound(soundKey);
						}
					});
			addField(labelButtonFieldEditor);
		}
	}
}
