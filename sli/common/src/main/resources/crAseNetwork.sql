---
-- ============LICENSE_START=======================================================
-- ONAP : CCSDK
-- ================================================================================
-- Copyright (C) 2017 ONAP
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ============LICENSE_END=========================================================
---

CREATE TABLE sdnctl.ASE (
	ase_network_id INT NOT NULL,
	topology VARCHAR(25),
	CONSTRAINT P_ASE PRIMARY KEY(ase_network_id));
	
CREATE TABLE sdnctl.ASE_PORT (
	esm_name VARCHAR(25),
	resource_emt_clli VARCHAR(25) NOT NULL,
	resource_emt_ip_addr VARCHAR(25) NOT NULL,
	port_action VARCHAR(25),
	profile VARCHAR(25) ,
	port VARCHAR(15) NOT NULL,
	state VARCHAR(25),
	resource_mode VARCHAR(25),
    speed INT,
    resource_lldp VARCHAR(1),
	resource_mtu VARCHAR(5),
	resource_autoneg VARCHAR(10),
	resource_twamp VARCHAR(10),
	resource_description VARCHAR(80),
	uni_circuit_id VARCHAR(45),
	CONSTRAINT P_ASE_PORT PRIMARY KEY(resource_emt_clli, port));
	
CREATE TABLE sdnctl.ASE_EVC (
	esm_name VARCHAR(25),
	emt_ip_addr VARCHAR(25) NOT NULL,
	evc_action VARCHAR(25),
	service_id VARCHAR(25),
	serv_type VARCHAR(25),
	evc_choice VARCHAR(25),
	uni_port VARCHAR(25) NOT NULL,
	lag_port VARCHAR(25),
	mac_onoff VARCHAR(25),	
	ppcos VARCHAR(25),
	cir VARCHAR(25),
	cbs VARCHAR(25),
	ebs VARCHAR(25),	
	sgos VARCHAR(25),
	pe VARCHAR(25),
	unit VARCHAR(25),
	qinq VARCHAR(25),	
	interface VARCHAR(25),
	evc_description VARCHAR(80),	
	bandwidth VARCHAR(10),
	svlan VARCHAR(5),
	cvlan VARCHAR(5),
	routing_instance VARCHAR(25),
	rd VARCHAR(25),
	rt VARCHAR(25),
	evc_limit VARCHAR(25),
	label_block_size VARCHAR(25),
	site VARCHAR(25),
	int_mac_limit VARCHAR(5),
	sgos_grade VARCHAR(25),
	bum_rate VARCHAR(25),
	uni_circuit_id VARCHAR(45),
	leg INT,
	CONSTRAINT P_ASE_EVC PRIMARY KEY(emt_ip_addr, uni_port,leg));
	
	
	
