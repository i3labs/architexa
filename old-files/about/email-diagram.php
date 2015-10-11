<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head><title>Architexa Feedback</title></head>
<body>

<p>Thanks for the feedback!</p>

<?php
	//$pear_user_config = '/home/archiexaweb/.pearrc';
	set_include_path(
		get_include_path() . 
		PATH_SEPARATOR . '/home/architexaweb/pear/php'
	);
	require_once "Mail.php";
	require_once "Mail/mime.php";
	
	$agent = $_SERVER['HTTP_USER_AGENT'];
	$uri = $_SERVER['REQUEST_URI'];
	$ip = $_SERVER['REMOTE_ADDR'];
	$dtime = date('r');


	$fp = fopen("logs/emails.txt", "a");

	// date time first
	fputs($fp, $dtime);

	// form info
	if ($_POST['diagramImage'] == "") {
		fputs($fp, " | No Submission");
		echo "<br>No Content";
	} else {
		$fromEmail = $_POST['email'];
		fputs($fp, " | FROM EMail: $fromEmail");
		$toEmail = $_POST['to'];
		fputs($fp, " | TO EMail: $toEmail");

		$msg = $_POST['body'];
		fputs($fp, " | Body: $msg");

		$sub = $_POST['subj']; 
		fputs($fp, " | Feedback subject: $sub");
		
		if ($fromEmail =="") $email = "notLoggedIn@architexa.com";
		// send e-mail
		$from = $fromEmail;
		$to = $toEmail;
		$subj = $sub;
		$body = $msg;
		
							
		$headers = array (
				'From' => $from, // google overrites this to be username
				'To' => $to,
				'Subject' => $subj);

		$mime = new Mail_mime();
		$mime->setTXTBody($body);
		//$mime->setHTMLBody("<html>" . $body . "</html>");
		
		$file = base64_decode($_POST['diagramImage']);                                    // Content of the file
        	$file_name = "image.png";                                   // Name of the Attachment
        	$content_type = "image/png";                                // Content type of the file
        	$attach = $mime->addAttachment ($file, $content_type, $file_name, 0);  // Add the attachment to the email
		if (PEAR::isError($attach)) {
			fputs($fp, " | MAIL ERROR: $attach->getMessage()");
			echo "<br><br><b>Error:</b> " . $attach->getMessage();
		}


			$bodyM = $mime->get();
		$headers = $mime->headers($headers);
		
		$smtp = Mail::factory("mail");
	      
		//$mail = $smtp->send($to, $headers, $body);
		$mail = $smtp->send($to, $headers, $bodyM);
		//$mail->send("best@friend.com", $headers, $body);

		
		if (PEAR::isError($mail)) {
			fputs($fp, " | MAIL ERROR: $mail->getMessage()");
			echo "<br><br><b>Error:</b> " . $mail->getMessage();
		}

	}

	// additional information
	$entry_line = " | IP: $ip | Agent: $agent  | URL: $uri \n";
	fputs($fp, $entry_line);


	fclose($fp);
?>

</body></html>

