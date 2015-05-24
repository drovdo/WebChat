var theMessage = function(user, text) {
	return {
		text: text,
		user: user
	};
};

var mainUrl = "/chat";
var hidden, visibilityChange;
var newMessageCounter = 0;
var messageList = [];

function run(){
	var appContainer = document.getElementsByClassName("chat")[0];
	appContainer.addEventListener("click", delegateEvent);
	appContainer.addEventListener("keydown", delegateEvent);
	$("title").text("<3 Chatty");
	if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support
		hidden = "hidden";
		visibilityChange = "visibilitychange";
	} else if (typeof document.mozHidden !== "undefined") {
		hidden = "mozHidden";
		visibilityChange = "mozvisibilitychange";
	} else if (typeof document.msHidden !== "undefined") {
		hidden = "msHidden";
		visibilityChange = "msvisibilitychange";
	} else if (typeof document.webkitHidden !== "undefined") {
		hidden = "webkitHidden";
		visibilityChange = "webkitvisibilitychange";
	}
	document.addEventListener(visibilityChange, handleVisibilityChange);
	var name = restoreLocal("chat username") || "";
	setName(name);
	if (name != "")
		document.getElementById("changeNameBtn").innerHTML = "Change name";
	else
		$("#enterName").attr("placeholder", "Type your name here");
	poll(true);
}

function handleVisibilityChange() {
	if (!document[hidden]) {
		$("title").text("<3 Chatty");
		newMessageCounter = 0;
	}
}

function createMessage(message) {
	if (message.user == document.getElementById("name").innerHTML)
		message.userFlag = true;
	else
		message.userFlag = false;
	addMessage(message);
}

function setName(name) {
	document.getElementById("name").innerHTML = name;
}

function delegateEvent(evtObj) {
	if(evtObj.type == "click" &&  evtObj.target.getAttribute("id") == "sendBtn") {
		onSendButtonClick(evtObj);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "changeNameBtn") {
		onChangeNameButtonClick(evtObj);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "iconDelete") {
		var message = evtObj.target.parentNode;
		onIconDeleteClick(message);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "iconEdit") {
		var message = evtObj.target.parentNode;
		onIconEditClick(message);
	}
	if(evtObj.type == "keydown" && evtObj.keyCode == 13 && document.activeElement.id == "enterName") {
		onChangeNameButtonClick(evtObj);
	}
	if(evtObj.type == "keydown" && evtObj.shiftKey == false && evtObj.keyCode == 13 && document.activeElement.id == "messageText") {
		evtObj.preventDefault();
		onSendButtonClick(evtObj);
	}
	if(evtObj.type == "keydown" && evtObj.keyCode == 38 && document.activeElement.id == "messageText") {
		var name = document.getElementById("name").innerHTML;
		var i;
		for (i = messageList.length - 1; i >= 0; i--)
			if (messageList[i].user == name)
				break;
		if (i >= 0) {
			var message = document.getElementById(messageList[i].id);
			onIconEditClick(message);
		}
	}
}

function onIconDeleteClick(message) {
	var conf = confirm("Confirm removal, please :)");
	if (conf == true) {
		var i;
		for (i = 0; i < messageList.length; i++)
			if (messageList[i].id == message.id)
				break;
		deleteFromServer(messageList[i]);
	}
}

var messageToEdit;

function onIconEditClick(message) {
	var messageArea = document.getElementById("messageText");
	messageArea.focus();
	messageArea.value = message.getElementsByTagName("div")[0].getElementsByTagName("span")[1].innerHTML;
	var sendButton = document.getElementById("sendBtn");
	sendBtn.innerHTML = "Edit";
	messageToEdit = message;
}

function onSendButtonClick(){
	if (document.getElementById("name").value == "")
		alert("Enter your name, please :)");
	else if (document.getElementById("messageText").value == "")
		alert("Your message is empty! :(");
	else if (document.getElementById("sendBtn").innerHTML != "Edit") {
		var message = theMessage(document.getElementById("name").value, document.getElementById("messageText").value);
		document.getElementById("messageText").value = "";
		addToServer(message);
	}
	else {
		document.getElementById("sendBtn").innerHTML = "Send";
		var text = document.getElementById("messageText").value;
		document.getElementById("messageText").value = "";
		var i;
		for (i = 0; i < messageList.length; i++)
			if (messageList[i].id == messageToEdit.id)
				break;
		messageList[i].text = text;
		editOnServer(messageList[i]);
	}
}
function onChangeNameButtonClick() {
	var name = document.getElementById("enterName")
	if (name != "")
	{
		document.getElementById("changeNameBtn").innerHTML = "Change name";
		$("#enterName").attr("placeholder", "");
	}
	else
		$("#enterName").attr("placeholder", "Type your name here");
	setName(name.value);
	storeLocal("chat username", name.value);
	name.value = "";
	$(".messageArea").empty();
	var tempMessage = messageList;
	messageList = [];
	for (var i = 0; i < tempMessage.length; i++)
		createMessage(tempMessage[i]);
}

function addMessage(message){
	if (message.userFlag == true) {
		var divItem = document.createElement("div");
		var divText = document.createElement("div");
		var spanName = document.createElement("span");
		var spanText = document.createElement("span");
		var deleteImg = document.createElement("img");
		var editImg = document.createElement("img");
		divItem.classList.add("userMessage");
		divItem.id = message.id;
		spanName.classList.add("userName");
		deleteImg.id =  "iconDelete";
		deleteImg.setAttribute("src", "resources/images/delete.png");
		deleteImg.setAttribute("alt", "Delete message");
		editImg.id = "iconEdit";
		editImg.setAttribute("src", "resources/images/edit.png");
		editImg.setAttribute("alt", "Edit message");
		spanName.innerHTML = message.user + ":&nbsp";
		spanText.innerHTML = message.text;
		divText.appendChild(spanName);
		divText.appendChild(spanText);
		divItem.appendChild(divText);
		divItem.appendChild(deleteImg);
		divItem.appendChild(editImg);
	}
	else {
		var divItem = document.createElement("div");
		var spanName = document.createElement("span");
		var spanText = document.createElement("span");
		divItem.classList.add("friendMessage");
		divItem.id = message.id;
		spanName.classList.add("friendName");
		spanName.innerHTML = message.user + ":&nbsp";
		spanText.innerHTML = message.text;
		divItem.appendChild(spanName);
		divItem.appendChild(spanText);
	}
	var items = document.getElementsByClassName("messageArea")[0];
	items.appendChild(divItem);
	if (document[hidden]) {
		newMessageCounter++;
		if (newMessageCounter == 1)
			$("title").text(newMessageCounter + " new message");
		else
			$("title").text(newMessageCounter + " new messages");
		var sound = document.getElementById("sound");
		sound.play();
	}
	messageList.push(message);
	var items = $(".messageArea");
	items.scrollTop(items[0].scrollHeight - items.height());
}

function deleteMessage(message) {
	message.parentNode.removeChild(message);
	var i;
	for (i = 0; i < messageList.length; i++)
		if (messageList[i].id == message.id)
			break;
	var dMessage = messageList[i];
	messageList.splice(i, 1);
	return dMessage;
}

function editMessage(message, text) {
	if (message.className != "friendMessage")
		message.getElementsByTagName("div")[0].getElementsByTagName("span")[1].innerHTML = text;
	else
		message.getElementsByTagName("span")[1].innerHTML = text;
	var i;
	for (i = 0; i < messageList.length; i++)
		if (messageList[i].id == message.id)
			break;
	messageList[i].text = text;
	return messageList[i];
}

function storeLocal(lsName, toSave) {
	if(typeof(Storage) == "undefined") {
		alert("localStorage is not accessible!");
		return;
	}
	localStorage.setItem(lsName, JSON.stringify(toSave));
}

function restoreLocal(lsName) {
	if(typeof(Storage) == "undefined") {
		alert("localStorage is not accessible");
		return;
	}
	var item = localStorage.getItem(lsName);
	return item && JSON.parse(item);
}

function addToServer(message) {
	$.ajax({
		url: mainUrl,
		type: 'POST',
		data: JSON.stringify(message),
		success: function (data) {
		},
		error: function () {
		}
	});
}

function editOnServer(message) {
	$.ajax({
		url: mainUrl,
		type: 'PUT',
		data: JSON.stringify(message),
		success: function (data) {
		},
		error: function () {
		}
	});
}

function deleteFromServer(message) {
	$.ajax({
		url: mainUrl,
		type: 'DELETE',
		data: JSON.stringify(message),
		success: function (data) {
		},
		error: function () {
		}
	});
}

var errorFlag = true;

function fatalError() {
	$("#serverOn").css({"color" : "red", "font-size" : "large"});
	$("#serverOn").text("SERVER IS UNAVAILIBLE!");
	if (errorFlag == true) {
		errorFlag = false;
		var conf = confirm("Server seems to shut down.\nThe message history is not valid any more :(\nWould you like to delete it?");
		if (conf)
			$(".messageArea").empty();
		messageList = [];
	}

}

function poll(isHistory) {
	$.ajax({
		url: mainUrl + "?flag=" + isHistory,
		type: 'GET',
		dataType: 'json',
		success: function(responseText){
			$("#serverOn").css({"color" : "RGB(44, 77, 55)", "font-size" : "medium"});
			$("#serverOn").text("Server is availible!");
			if (!errorFlag)
				$(".messageArea").empty();
			errorFlag = true;
			var messages = responseText.messages;
			for (var i = 0; i < messages.length; i++) {
				if (messages[i].actionToDo == "POST") {
					createMessage(messages[i]);
				}
				else if (messages[i].actionToDo == "DELETE") {
					var message = document.getElementById(messages[i].id);
					deleteMessage(message)
				}
				else if (messages[i].actionToDo == "PUT") {
					var message = document.getElementById(messages[i].id);
					editMessage(message, messages[i].text);
				}
			}
		},
		error: function(){
			fatalError();
		},
		complete: function() {
			setTimeout(function() { poll(!errorFlag) }, 1000);
		}
	});
};