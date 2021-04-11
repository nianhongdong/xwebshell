<!DOCTYPE html>
<html>
<head>
	<title>${title}</title>
	<!--jQuery核心框架库 -->
    <script type="text/javascript" src="${base}/static/assets/js/jquery-1.11.3-min.js"></script>
    <script type="text/javascript" src="${base}/static/assets/js/jquery-migrate-1.4.1.min.js"></script>
    <!--jQuery浏览器检测 -->
    <script type="text/javascript" src="${base}/static/js/browse/browse-judge.js"></script>
    <script type="text/javascript">
    	//\u6d4f\u89c8\u5668\u7248\u672c\u9a8c\u8bc1
    	/*var broswer = broswer();
    	if(broswer.msie==true||broswer.safari==true||broswer.mozilla==true||broswer.chrome==true){
    		if(broswer.msie==true&&broswer.version<9){
    		   window.location.href = _path+"/xtgl/init_cxBrowser.html";
    		}
    	}else{
    		 window.location.href = _path+"/xtgl/init_cxBrowser.html";
    	}*/
    </script>
	<!--Bootstrap布局框架-->
    <link rel="stylesheet" type="text/css" href="${base}/static/assets/plugins/bootstrap/css/bootstrap.min.css" />
    <script type="text/javascript" src="${base}/static/assets/plugins/bootstrap/js/bootstrap.min.js" charset="utf-8"></script>

	<style>
	.nav-tabs>li.active>a, .nav-tabs>li.active>a:focus, .nav-tabs>li.active>a:hover{
		    color: #eee;
		    background-color: #000000;
	};
	.nav-tabs>li>{
		background-color: #eee;
	}
	.nav-tabs>li>a {
	    color: #212529;
	    background-color: #ddd;
	    text-decoration: none;
	}
	</style>
</head>
<body>
	<ul id="myTab" class="nav nav-tabs">
		[#list webShellURLVOList as vo]
	    <li>
	        <a href="#${vo.serverId}" data-toggle="tab">${vo.title}</a>
	    </li>
		[/#list]
	</ul>
	<div id="myTabContent" class="tab-content" >
		[#list webShellURLVOList as vo]
		    <div class="tab-pane fade" id="${vo.serverId}" style="height: 100%;">
		        <iframe name="frame${vo.serverId}" data-src="${vo.url}" src="" style="width:100%;height:100%;border:0;" frameborder="0">
				</iframe>
		    </div>
	    [/#list]
	</div>
	<script type="text/javascript">
	
		var full_height = $(document).outerHeight();
		var head_height = $("#myTab").outerHeight();
	
		//webshell高度自适应
		var webshell_height = full_height - head_height - 5;
		$("#myTabContent").height(webshell_height);
		
		//追加 X 按钮
		$("#myTab a").each(function(i,n){
			var $n = $(n);
			
			var h = $n.parent().outerHeight();
			var w = $n.parent().outerWidth();
			
			var p_x = w - 15;
			var style = "color:red;cursor:pointer;position:absolute;top:5px;left:"+p_x+"px";
			
			$n.after('<span class="close-webshell" style='+ style +'> X </span>');
		});
		
		$(".close-webshell").off("click").on("click",function(event){
		
			event.stopPropagation();
			
			$li = $(this).closest("li");
			var index = $li.index();
			$("#myTabContent").find("div").eq(index).remove();
			$li.remove();
			
			//回复其他标签的打开状态
			$("#myTab a:first").click();
		});
	
		$("#myTab a").off("click").on("click",function(){
		
			var $this = $(this);
			
			var index = $this.closest("li").index();
			
			var $pane = $("#myTabContent .tab-pane").eq(index);
			
			$this.siblings().removeClass("active");
			$pane.siblings().removeClass("active");
			
			$this.addClass("active");
			$pane.addClass("active");
			
			var $i = $pane.find("iframe");
			var src = $i.attr("src");
			if(!src){
				$i.attr("src",$i.attr("data-src"));		
			}
		});
	
		$("#myTab a:first").click();

	</script>
</body>
</html>