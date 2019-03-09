<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
	
		$connection = new PDO($host, $user, $pass);
		
		$cid = $_POST['cid'];
				
		//set settings if key is inserted
		$stmtKey = $connection->prepare("SELECT `key_inserted` as keyState FROM `settings` WHERE `counter_id` = '$cid'");
		$stmtKey->execute();

		if($stmtKey->fetch()[0] == 2)
		{
			$straitTime = $_POST['straitTime'];
			$portionTime = $_POST['portionTime'];
			$maxPortionTime = $_POST['maxPortionTime'];
			$volume = $_POST['volume'];
			$gss1 = $_POST['gss1'];
			$gss2 = $_POST['gss2'];
			$gss1max = $_POST['gss1max'];
			$gss2max = $_POST['gss2max'];
		
			$stmtSettings = $connection->prepare("
			UPDATE `settings` SET
			`s_time` = '$straitTime', 
			`p_time` = '$portionTime', 
			`mp_time` = '$maxPortionTime', 
			`volume` = '$volume',
			`gss1` = '$gss1',
			`gss2` = '$gss2',
			`gss1m` = '$gss1max',
			`gss2m` = '$gss2max'
			WHERE `counter_id` = '$cid'");
			$stmtSettings->execute();
		}
		else
		{
			header("HTTP/1.1 409 Conflict");
		}

		$connection = null;
	}