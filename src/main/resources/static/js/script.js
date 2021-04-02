console.log("hiii");

const togglesidebar = ()=>{

 

	if($(".sidebar").is(":visible")){
	
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
		
	}
	else{
	 // show sidebar
	 
		$(".sidebar").css("display","flex");
		
	}
};

function phonenumber(inputtxt,e)
{
console.log(e);
  let phone = /^\d{10}$/;
  if((inputtxt.value.match(phone))) {
          return true;
      }
        
      else
        {
        alert("please enter valid mobile no.");
     	e.preventDefault();
        return false;
        }
}
