<?php
 header('Access-Control-Allow-Origin: *');
  header('Content-Type: application/json');
  header('Access-Control-Allow-Methods: POST');
  header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization,X-Requested-With');
   //get row posted data
   $data =json_decode(file_get_contents("php://input"));
   $checkoutRequestId =$data->CheckoutRequestID;  
$link = mysqli_connect("localhost", "username", "password", "database");

if($link === false){
    die("ERROR: Could not connect. " . mysqli_connect_error());
}
// Attempt select query execution
$sql = "SELECT MerchantRequestID, CheckoutRequestID, ResultCode, ResultDesc FROM payment_transactions WHERE  CheckoutRequestID = '$checkoutRequestId' ";


if($result = mysqli_query($link, $sql)){
    if(mysqli_num_rows($result) > 0){
       $posts_arr =array();
   $posts_arr = array('status' => "0" );
   $posts_arr['message'] = "Record Sucessfully fetched.";
   $posts_arr['payment'] =array();

        while($row = mysqli_fetch_array($result)){
      $post_item =array(
      'MerchantRequestID' => $row['MerchantRequestID'],
      'CheckoutRequestID' =>$row['CheckoutRequestID'],
      'ResultCode'=>$row['ResultCode'],
      'ResultDesc' =>$row['ResultDesc']
    );
    //push to data
    array_push($posts_arr['payment'],$post_item);
        
      }
      echo json_encode($posts_arr);
        mysqli_free_result($result);
    } else{
       $posts_arr = array('status' => "1" );
       $posts_arr['message'] = "No records matching your query were found.";
       $posts_arr['payment'] =array();

        echo json_encode($posts_arr);
        // echo "No records matching your query were found.";
    }
} else{
   $posts_arr = array('status' => "2" );
      $posts_arr['message'] = "ERROR: Could not able to execute your request" . mysqli_error($link);
      $posts_arr['payment'] =array();
echo json_encode($posts_arr);
    // echo "ERROR: Could not able to execute $sql. " . mysqli_error($link);
}
 
// Close connection
mysqli_close($link);



