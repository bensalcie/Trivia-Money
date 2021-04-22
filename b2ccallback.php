<?php 
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: *");
header('Access-Control-Allow-Headers: Origin, X-Requested-With,X-Auth-Token, Content-Type, Accept');


$stkCallbackResponse = file_get_contents('php://input');

$ipn_data = json_decode($stkCallbackResponse,true);
$new_status = 'COMPLETED';

$result_code = $ipn_data['Body']['stkCallback']['ResultCode'];
$checkout_request_id = $ipn_data['Body']['stkCallback']['CheckoutRequestID'];
$merchant_request_id = $ipn_data['Body']['stkCallback']['MerchantRequestID'];
$result_description = $ipn_data['Body']['stkCallback']['ResultDesc'];

if($result_code != 0){
	$new_status = 'CANCELLED';
}else{
    $mpesa_confirmation_code = $ipn_data['Body']['stkCallback']['CallbackMetadata']['Item']['1']['Value'];
}

$link = mysqli_connect("localhost", "username", "password", "database");
//  $fp = fopen('resp.txt', 'w');

// Check connection
if($link === false){
    die("ERROR: Could not connect. " . mysqli_connect_error());
}
 
// Attempt insert query execution
$sql = "INSERT INTO payment_transactions (MerchantRequestID
, CheckoutRequestID, ResultCode,ResultDesc) VALUES
            ('$merchant_request_id', '$checkout_request_id', '$result_code','$result_description')";
if(mysqli_query($link, $sql)){
    echo "Records added successfully.";
    // fwrite($fp,'Response: sucess ');

} else{
        // fwrite($fp,'Response: error :'. mysqli_error($link));

    echo "ERROR: Could not able to execute $sql. " . mysqli_error($link);
} 
// Close connection
mysqli_close($link);


    // $content = $TransactionType."\n".$ReceiptNumber."\n".$TransTime ."\n".$Amount."\n".$BusinessShortCode."\n".$BillRefNumber."\n".$InvoiceNumber."\n".$OrgAccountBalance."\n".$ThirdPartyTransID."\n".$MSISDN ."\n".$FirstName."\n".$MiddleName."\n".$LastName;
// fwrite($fp,'Response: '.$stkCallbackResponse);
// fclose($fp);
// saveC2BTransaction($BillRefNumber,"Credit",$source,"PAY HERO KENYA LTD",$Amount,$ReceiptNumber,$OrgAccountBalance,$date);
?>
