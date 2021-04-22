<?php 
//This file is used to receive the B2C callback resonse all the important parameters have been decoded for your consumption.you can perform your logic below.
include 'all.php';
$response                            = json_decode(file_get_contents('php://input'), true);;
$Result                              = $response["Result"];
$ResultType                          = $Result["ResultType"];
$ResultCode                          = $Result["ResultCode"];
$ResultDesc                          = $Result["ResultDesc"];
$OriginatorConversationID            = $Result["OriginatorConversationID"];
$ConversationID                      = $Result["ConversationID"];
$TransactionID                       = $Result["TransactionID"];
$ResultParameters                    = $Result["ResultParameters"];
$ResultParameter                     = $Result["ResultParameters"]["ResultParameter"];
$TransactionReceipt                  = $ResultParameter[1]["Value"];
$TransactionAmount                   = $ResultParameter[0]["Value"];
$B2CWorkingAccountAvailableFunds     = $ResultParameter[5]["Value"];
$B2CUtilityAccountAvailableFunds     = $ResultParameter[4]["Value"];
$TransactionCompletedDateTime        = $ResultParameter[3]["Value"];
$ReceiverPartyPublicName             = $ResultParameter[2]["Value"];
$B2CChargesPaidAccountAvailableFunds = $ResultParameter[7]["Value"];
$B2CRecipientIsRegisteredCustomer    = $ResultParameter[6]["Value"];
$ReferenceData                       = $response["ReferenceData"];
$ReferenceItem                       = $ReferenceData["ReferenceItem"];
$QueueTimeoutURL                     = $ReferenceItem[0]["Value"];
file_put_contents('./logs/log_b2c_'.date("j.n.Y").'.log', json_encode($response)."\n", FILE_APPEND);

//Do your logic here
if ($ResultCode==0) {
	# code...if transaction was succesful
	$date=date('Y-m-d');
	processB2C($OriginatorConversationID,"Debit","PAY HERO KENYA LTD",$ReceiverPartyPublicName,$TransactionAmount,$TransactionID,$B2CUtilityAccountAvailableFunds,$date,$ResultDesc);
}

if ($ResultCode>0) {
	# code...if it failed.
	FailedB2C($OriginatorConversationID,$ResultDesc);
}
 ?>