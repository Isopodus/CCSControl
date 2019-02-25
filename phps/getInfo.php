<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		$connection = new PDO($host, $user, $pass);
		
		$date = $_POST['date'];
		$user = $_POST['user'];
		
		//output data
		$outputArray = array();
		
		//get all counters of user
		$stmt = $connection->prepare("SELECT `counter` FROM `counter_u` WHERE `user` = '$user'");
		$stmt->execute();
		while($counter = $stmt->fetch(PDO::FETCH_ASSOC))
		{
			//get data about this counter
			$cid = $counter['counter'];
			$stmtValues = $connection->prepare("SELECT SUM(p1 + p2) as portions, SUM(s1 + s2) as straits FROM `$cid` WHERE DATE(`date_time`) = '$date'");
			$stmtValues->execute();
			
			$stmtTime = $connection->prepare("SELECT `sync_time` as sync, `startup_time` as start FROM `status` WHERE `counter_id` = '$cid'");
			$stmtTime->execute();

			$row = array('counter' => $cid, 'values' => $stmtValues->fetch(PDO::FETCH_ASSOC), 'time' => $stmtTime->fetch(PDO::FETCH_ASSOC));
			array_push($outputArray, $row);
		}		

		//return json data
		//var_dump($outputArray);
		echo json_encode($outputArray);
		
		$connection = null;
	}