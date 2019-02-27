<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		$connection = new PDO($host, $user, $pass);
		
		$cid = $_POST['cid'];	
		
		//get settings
		$stmtSettings = $connection->prepare("
		SELECT 
		`s_time` as straitTime, 
		`p_time` as portionTime, 
		`mp_time` as maxPortionTime, 
		`volume` as volume,
		`gss1` as gss1,
		`gss2` as gss2,
		`gss1m` as gss1max,
		`gss2m` as gss2max
		FROM `settings` WHERE `counter_id` = '$cid'");
		$stmtSettings->execute();
		$result = $stmtSettings->fetch(PDO::FETCH_ASSOC);

		echo json_encode($result);
		
		$connection = null;
	}