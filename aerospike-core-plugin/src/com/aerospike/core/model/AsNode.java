/* 
 * Copyright 2012-2015 Aerospike, Inc.
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
package com.aerospike.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aerospike.client.cluster.Node;


public class AsNode implements IAsEntity{
	String 	nodeID;
	String 	build;
	String	version;
	String  address;
	int		port;
	boolean online = false;
	protected transient Map<String, NameValuePair> stats = null;
	private Map<String, AsNameSpace> nameSpaces = new HashMap<String, AsNameSpace>();
	private Object parent;
	private String name;
	private String clusterGeneration;

	public AsNode(Object parent, String name) {
		this.parent = parent;
		this.name = name;
		String[] parts = this.name.split(":");
		address = parts[0];
		port = Integer.parseInt(parts[1]);
	}

	public AsNode(Object parent, Node node) {
		this.parent = parent;
		this.name = node.getHost().toString();
		address = node.getHost().name;
		port = node.getHost().port;
	}

	@Override
	public String getName() {
		return this.name;
	}


	@Override
	public boolean hasChildren() {
		return (nameSpaces != null && nameSpaces.size() > 0);
	}


	@Override
	public Object[] getChildren() {
		Object[] kids = new Object[this.nameSpaces.values().size() + 6];
		kids[0] = new NameValuePair(this, "Node ID", getNodeID());
		kids[1] = new NameValuePair(this, "Build", getBuild());
		kids[2] = new NameValuePair(this, "Cluster Size", getClusterSize());
		kids[3] = new NameValuePair(this, "Free Memory", getFreeMemory());
		kids[4] = new NameValuePair(this, "Free Disk", getFreeDisk());
		kids[5] = new NameValuePair(this, "Migrations", getMigration());
		int index = 6;
		for (Object kid : this.nameSpaces.values()){
			kids[index] = kid;
			index++;
		}
		return kids;
	}


	@Override
	public Object getParent() {
		return this.parent;
	}

	@Override
	public String toString() {
		return this.name ;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AsNode){
			AsNode target = (AsNode) obj;
			return (target.address.equals(this.address) && target.port == this.port);
		} else {
			return false;
		}
	}


	public Map<String, AsNameSpace> getNameSpaces() {
		return nameSpaces;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public void setDetails(HashMap<String, String> info){
		setStatistics(info.get("statistics"));
		setNodeID(info.get("node"));
		setBuild(info.get("build"));
		setVersion(info.get("version"));
		setClusterGeneration(info.get("cluster-generation"));
	}

	public void setClusterGeneration(String clusterGeneration) {
		this.clusterGeneration = clusterGeneration;
	}
	
	public String getClusterGeneration() {
		return clusterGeneration;
	}

	public void setStatistics(String info){
		/*
		cluster_size=2;cluster_key=6FBF3542EBE77019;cluster_integrity=true;objects=5002;total-bytes-disk=474114686976;
		used-bytes-disk=3166720;free-pct-disk=99;total-bytes-memory=15032385536;used-bytes-memory=320384;
		data-used-bytes-memory=0;index-used-bytes-memory=320128;sindex-used-bytes-memory=256;free-pct-memory=99;
		stat_read_reqs=2735;stat_read_success=192;stat_read_errs_notfound=2543;stat_read_errs_other=0;stat_read_latency_gt50=0;
		stat_read_latency_gt100=0;stat_read_latency_gt250=0;stat_write_reqs=2743;stat_write_reqs_xdr=0;stat_write_success=2742;
		stat_write_errs=1;stat_write_latency_gt50=0;stat_write_latency_gt100=0;stat_write_latency_gt250=0;stat_delete_success=1;
		stat_rw_timeout=0;udf_read_reqs=0;udf_read_success=0;udf_read_errs_other=0;udf_read_latency_gt50=0;udf_read_latency_gt100=0;
		udf_read_latency_gt250=0;udf_write_reqs=0;udf_write_success=0;udf_write_err_others=0;udf_write_latency_gt50=0;
		udf_write_latency_gt100=0;udf_write_latency_gt250=0;udf_delete_reqs=0;udf_delete_success=0;udf_delete_err_others=0;
		udf_lua_errs=0;udf_scan_rec_reqs=0;udf_query_rec_reqs=0;udf_replica_writes=0;stat_proxy_reqs=0;stat_proxy_reqs_xdr=0;
		stat_proxy_success=0;stat_proxy_errs=0;stat_proxy_latency_gt50=0;stat_proxy_latency_gt100=0;stat_proxy_latency_gt250=0;
		stat_expired_objects=0;stat_evicted_objects=0;stat_deleted_set_objects=0;stat_evicted_set_objects=0;stat_evicted_objects_time=0;
		stat_single_bin_records=0;stat_zero_bin_records=0;stat_zero_bin_records_read=0;stat_nsup_deletes_not_shipped=1;err_tsvc_requests=1;
		err_out_of_space=0;err_duplicate_proxy_request=0;err_rw_request_not_found=0;err_rw_pending_limit=0;err_rw_cant_put_unique=0;
		err_write_empty_writes=0;err_rcrb_reduce_gt5=0;err_rcrb_reduce_gt50=0;err_rcrb_reduce_gt100=0;err_rcrb_reduce_gt250=0;
		fabric_msgs_sent=21788;fabric_msgs_rcvd=21788;paxos_principal=BB9F87671211B00;migrate_msgs_sent=8204;migrate_msgs_recv=16398;
		migrate_progress_send=0;migrate_progress_recv=0;migrate_num_incoming_accepted=4096;migrate_num_incoming_refused=0;queue=0;
		transactions=5782;reaped_fds=0;scan_initiate=0;tscan_initiate=1;scan_pending=0;tscan_pending=0;tscan_succeeded=1;
		tscan_aborted=0;batch_initiate=0;batch_queue=0;batch_tree_count=0;batch_timeout=0;batch_errors=0;info_queue=0;proxy_initiate=0;
		proxy_action=0;proxy_retry=0;proxy_retry_q_full=0;proxy_unproxy=0;proxy_retry_same_dest=0;proxy_retry_new_dest=0;
		write_master=5478;write_prole=5276;read_dup_master=0;read_dup_prole=0;rw_err_dup_internal=0;rw_err_dup_cluster_key=0;
		rw_err_dup_send=0;rw_err_dup_write_internal=0;rw_err_dup_write_cluster_key=0;rw_err_write_internal=0;rw_err_write_cluster_key=0;
		rw_err_write_send=0;rw_err_ack_internal=0;rw_err_ack_nomatch=0;rw_err_ack_badnode=0;client_connections=1;waiting_transactions=0;
		tree_count=0;record_refs=5002;record_locks=0;migrate_tx_objs=0;migrate_rx_objs=0;write_reqs=0;storage_queue_full=0;
		storage_queue_delay=0;partition_actual=2060;partition_replica=2036;partition_desync=0;partition_absent=0;partition_object_count=5002;
		partition_ref_count=4096;system_free_mem_pct=97;system_sindex_data_memory_used=256;system_swapping=false;
		err_replica_null_node=0;err_replica_non_null_node=0;err_sync_copy_null_node=0;err_sync_copy_null_master=0;
		storage_defrag_records=0;err_storage_defrag_fd_get=0;storage_defrag_seek=0;storage_defrag_read=0;storage_defrag_bad_magic=0;
		storage_defrag_sigfail=0;storage_defrag_corrupt_record=0;storage_defrag_wait=0;err_write_fail_prole_unknown=0;
		err_write_fail_prole_generation=0;err_write_fail_unknown=0;err_write_fail_key_exists=0;err_write_fail_generation=0;
		err_write_fail_generation_xdr=0;err_write_fail_bin_exists=0;err_write_fail_parameter=1;err_write_fail_incompatible_type=0;
		err_write_fail_noxdr=0;err_write_fail_prole_delete=0;stat_duplicate_operation=0;uptime=88876;stat_write_errs_notfound=0;
		stat_write_errs_other=1;stat_leaked_wblocks=0;heartbeat_received_self=591417;heartbeat_received_foreign=591419;query_reqs=0;
		query_success=0;query_abort=0;query_fail=0;query_avg_selectivity=0;query_queue_full=0;query_agg=0;query_agg_success=0;
		query_agg_abort=0;query_agg_avg_selectivity=0;query_lookups=0;query_lookup_success=0;query_lookup_abort=0;
		query_lookup_avg_selectivity=0
		 */
		if (!info.isEmpty()){
			String[] parts = info.split(";");
			if (stats == null){
				stats = new HashMap<String, NameValuePair>();
			} 
			for (String part : parts){
				String[] kv = part.split("=");
				String key = kv[0];
				String value = kv[1];
				NameValuePair stat = stats.get(key);
				if (stat == null){
					stat = new NameValuePair(this, key, value);
				}
				stat.value = value;
				stats.put(key, stat);
			}
		}
	}
	public List<NameValuePair> getStats(){
		List<NameValuePair> result = new ArrayList<NameValuePair>();
		Set<String> keys = this.stats.keySet();
		for (String key : keys){
			NameValuePair nvp = this.stats.get(key);
			result.add(nvp);
		}
		return result;
	}

	//	public void addNameSpace(AsNameSpace ns) {
	//		this.nameSpaces.put(ns.getName(), ns);
	//
	//	}

	public AsNameSpace fetchNameSpace(String name) {
		AsNameSpace ns = this.nameSpaces.get(name);
		if (ns == null){
			ns = new AsNameSpace(this, name);
			this.nameSpaces.put(ns.getName(), ns);
		}
		return ns;
	}

	public String getNodeID() {
		return this.nodeID;
//		if (stats!=null)
//			return (String)stats.get("cluster_key").value;
//		return "Not available";
	}

		public void setNodeID(String nodeID) {
			this.nodeID = nodeID;
		}
	
		public String getBuild() {
			return build;
		}
	
		public void setBuild(String build) {
			this.build = build;
		}
	
	public int getClusterSize(){
		if (stats!=null)
			return Integer.parseInt((String)stats.get("cluster_size").getValue());
		return 0;
	}
	public long	getObjects(){
		if (stats!=null)
			return Long.parseLong((String)stats.get("objects").getValue());
		return 0;
	}
	
		public String getVersion() {
			return version;
		}
	
		public void setVersion(String version) {
			this.version = version;
		}
	


	//	public List<NameValuePair> getStatsAsNameValue(){
	//		List<NameValuePair> list = new ArrayList<NameValuePair>();
	//		for (String key : this.stats.keySet()){
	//			list.add(new NameValuePair(key, this.stats.get(key)));
	//		}
	//		return list;
	//	}
	//
	public String getMigration() {
		if (stats!=null)
			return String.format("(%s, %s)", stats.get("migrate_progress_send").value, stats.get("migrate_progress_recv").value);
		return "not available";
	}

	public int getFreeMemory() {
		if (stats!=null)
			return Integer.parseInt((String)stats.get("free-pct-memory").value);
		return -1;
	}

	public int getFreeDisk() {
		if (stats!=null)
			return Integer.parseInt((String)stats.get("free-pct-disk").value);
		return -1;
	}

	public int getFreeSystemMemory() {
		if (stats!=null)
			return Integer.parseInt((String)stats.get("system_free_mem_pct").value);
		return -1;
	}

	//	public AsNameSpace getNameSpace(String name){
	//		return nameSpaces.get(name);
	//	}
	//	public Collection<AsNameSpace> getNameSpaces(){
	//		return this.nameSpaces.values();
	//	}
	//
	//
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}




}