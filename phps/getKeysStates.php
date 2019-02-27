<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		$connection = new PDO($host, $user, $pass);
		
		$counters = json_decode($_POST['counters']);
		
		//output array
		$outputArray = array();
		foreach($counters as $value)
		{
			//get key state of this counter
			$cid = $value;
			$stmtKeys = $connection->prepare("SELECT `key_inserted` as keyState FROM `settings` WHERE `counter_id` = '$cid'");
			$stmtKeys->execute();

			array_push($outputArray, $stmtKeys->fetch(PDO::FETCH_ASSOC));
		}		

		echo json_encode($outputArray);
		
		$connection = null;
	}