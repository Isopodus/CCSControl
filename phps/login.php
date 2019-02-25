<?php
	if ($_SERVER['REQUEST_METHOD'] === 'POST') {
		require("config.php");
		
		$connection = new PDO($host, $user, $pass);

		$login = $_POST['login'];
		$password = $_POST['password'];
		
		//get password and salt
		$stmt = $connection->prepare("SELECT `password`, `salt` FROM `users` WHERE login='$login'");
		//$stmt = $connection->prepare("SELECT `login`, `password`, `salt` FROM `users`");
		$stmt->execute();
		$result = $stmt->fetch(PDO::FETCH_ASSOC);
		
		
		$saltedPassword = $result['password'];
		$salt = $result['salt'];

		//compare
		if($password == null)								//wrong login (query returned null)
		   echo 0;
		else if(md5($password . $salt) == $saltedPassword)//OK	
		   echo 1;
		else													//wrong password
		   echo -1;

		$connection = null;
	}