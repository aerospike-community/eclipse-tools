/* 
 * Copyright 2012-2014 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
		store.setDefault(PreferenceConstants.CLUSTER_CONNECTION_TIMEOUT,
				20);
		store.setDefault(PreferenceConstants.PORT,
				3000);
		store.setDefault(PreferenceConstants.UDF_PATH,
				"udf");
		store.setDefault(PreferenceConstants.GENERATION_PATH,
				"src/generated");
		store.setDefault(PreferenceConstants.AQL_PATH,
				"aql");
		store.setDefault(PreferenceConstants.AUTO_REFRESH,
				true);
		store.setDefault(PreferenceConstants.REFRESH_PERIOD,
				30);
	}

}
