<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		$connection = new PDO($host, $user, $pass);
		
		$cid = $_POST['cid'];
		$date = $_POST['date'];
	
		
		//get status
		$stmtStatus = $connection->prepare("SELECT `sync_time` as syncTime, `startup_time` as startupTime, `sd_status` as sdStatus, `inner_temp` as temp, `humid` FROM `status` WHERE `counter_id` = '$cid'");
		$stmtStatus->execute();
		$resultStatus = $stmtStatus->fetch(PDO::FETCH_ASSOC);
		
		$stmtPortions = $connection->prepare("SELECT SUM(p1 + p2) as portions, SUM(s1 + s2) as straits FROM `$cid` WHERE DATE(`date_time`) = '$date'");
		$stmtPortions->execute();
		$resultValues = $stmtPortions->fetch(PDO::FETCH_ASSOC);
		
		$output = array('status' => $resultStatus, 'values' => $resultValues);
		echo json_encode($output);
		
		$connection = null;
	}