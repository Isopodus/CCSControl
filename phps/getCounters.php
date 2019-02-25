<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		//get counters
		$connection = new PDO($host, $user, $pass);
		
		$user = $_POST['user'];
		$stmt = $connection->prepare("SELECT `counter` FROM `counter_u` WHERE `user` = '$user'");
		$stmt->execute();
		$result = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
		echo json_encode($result);
		
		$connection = null;
	}