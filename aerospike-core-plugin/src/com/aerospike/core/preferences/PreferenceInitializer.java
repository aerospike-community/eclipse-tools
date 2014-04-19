package com.aerospike.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aerospike.core.CoreActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.SEED_NODE,
				"127.0.0.1");
		store.setDefault(PreferenceConstants.PORT,
				3000);
		store.setDefault(PreferenceConstants.UDF_PATH,
				"udf");
		store.setDefault(PreferenceConstants.GENERATION_PATH,
				"generated");
	}

}
