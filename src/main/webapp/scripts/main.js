(function () {
    // PART1: get DOM elements from:
    // <div class="container"> -- <section class="main-section"> -- <div id="login-form">
    var oAvatar = document.getElementById('avatar'),
        oWelcomeMsg = document.getElementById('welcome-msg'),
        oLogoutBtn = document.getElementById('logout-link'),
        oRegisterFormBtn = document.getElementById('register-form-btn'),
        oLoginBtn = document.getElementById('login-btn'),
        oLoginForm = document.getElementById('login-form'),
        oLoginUsername = document.getElementById('username'),
        oLoginPwd = document.getElementById('password'),
        oLoginFormBtn = document.getElementById('login-form-btn'),
        oLoginErrorField = document.getElementById('login-error'),

        //<div class="container"> -- <section class="main-section"> -- <div id="register-form">
        oRegisterBtn = document.getElementById('register-btn'),
        oRegisterUsername = document.getElementById('register-username'),
        oRegisterPwd = document.getElementById('register-password'),
        oRegisterFirstName = document.getElementById('register-first-name'),
        oRegisterLastName = document.getElementById('register-last-name'),
        oRegisterForm = document.getElementById('register-form'),
        oRegisterResultField = document.getElementById('register-result'),

        //<div class="container"> -- <section class="main-section"> -- <div id="item-nav"> -- <div id="main-nav">
        oNearbyBtn = document.getElementById('nearby-btn'),
        oRecommendBtn = document.getElementById('recommend-btn'),
        oFavBtn = document.getElementById('fav-btn'),
        oNavBtnList = document.getElementsByClassName('main-nav-btn'),
        oItemNav = document.getElementById('item-nav'),
        oItemList = document.getElementById('item-list'),
        oTpl = document.getElementById('tpl').innerHTML, // here, the innerHMTL is text/html (the template for item-list)

        // default data
        userId = '1111',
        userFullName = 'John',
        lon = -122,
        lat = 47,

        // lon = -73.93,
        // lat = 40.73,

        // lon = -122.08,
        // lat = 37.38,

        // global variable to store data from serverExecutor
        itemArr;

    // PART2: entry function: use "init function"
    function init() {
        // validation session --> will be done after ajax
        // NOT INCLUDE： persistent login (by checking sessionID)
        validateSession();
        // PART3: bind events
        bindEvent();
    }

    function validateSession() {
        // functionality: to show login form + hide the rest elements
        switchLoginRegister('login');
        // DEBUG:
        console.log("validateSession ok!");

    }

    function bindEvent() {
        oRegisterFormBtn.addEventListener('click', function() {
            // console.log("click register!")
            switchLoginRegister('register');
        }, false);

        oLoginFormBtn.addEventListener('click', function() {
            // console.log("click login!")
            switchLoginRegister('login');
        }, false);

        // click login button
        oLoginBtn.addEventListener('click', loginExecutor, false);

        // click item button
        oItemList.addEventListener('click', changeFavoriteItem, false);

        // favorite button clicked
        oFavBtn.addEventListener('click', loadFavoriteItems, false);

        // Nearby button clicked
        oNearbyBtn.addEventListener('click', loadNearbyData, false);

        // Recommendation button clicked
        oRecommendBtn.addEventListener('click', loadRecommendedItems, false);

        // register button clicked
        oRegisterBtn.addEventListener('click', registerExecutor, false);
    }

    function switchLoginRegister(name) {
        // hide header elements
        showOrHideElement(oAvatar, 'none');
        showOrHideElement(oWelcomeMsg, 'none');
        showOrHideElement(oLogoutBtn, 'none');

        // hide item list area
        showOrHideElement(oItemNav, 'none');
        showOrHideElement(oItemList, 'none');

        // show or hide login/register
        if (name === 'login') {
            // case 1: name == login, hide register form
            showOrHideElement(oRegisterForm, 'none');
            // clear register error msg
            oRegisterResultField.innerHTML = '';
            // show login form
            showOrHideElement(oLoginForm, 'block');

        } else {
            // case 2: name == register, hide login
            showOrHideElement(oLoginForm, 'none');
            // clear login error msg
            oLoginErrorField.innerHTML = '';
            // show register
            showOrHideElement(oRegisterForm, 'block');
        }
    }

    /**
     * API Login
     */
    function loginExecutor() {
        // console.log("click login button");
        var username = oLoginUsername.value,
            password = oLoginPwd.value;

        if (username === "" || password === "") {
            oLoginErrorField.innerHTML = 'Please fill in all fields';
            return;
        }

        // else, use MD5 to encrypt the password and send to server
        password = md5(username + md5(password));
        // DEBUG:
        console.log("username and pwd:", username, password);

        // use ajax to send to server + server validate on user and password
        ajax({
            method: 'POST',
            url: './login',
            data: {
                user_id : username,
                password : password
            },
            success: function(result) {
                // console.log("result is here!");
                // console.log(result);
                if (result.status === 'OK') {
                    // show welcome message
                    welcomeMsg(result);
                    // fetch data
                    fetchData();
                } else {
                    oLoginErrorField.innerHTML = 'Invalid username or password';
                }
            },
            error: function() {
                throw new Error('Invalid username or password');
            }
        });
    }

    /**
     * API Register
     */
    function registerExecutor() {
        var username = oRegisterUsername.value,
            password = oRegisterPwd.value,
            firstName = oRegisterFirstName.value,
            lastName = oRegisterLastName.value;

        if (username === "" || password == "" || firstName === ""
            || lastName === "") {
            oRegisterResultField.innerHTML = 'Please fill in all fields';
            return;
        }

        if (username.match(/^[a-z0-9_]+$/) === null) {
            oRegisterResultField.innerHTML = 'Invalid username';
            return;
        }
        password = md5(username + md5(password));

        ajax({
            method: 'POST',
            url: './register',
            data: {
                user_id : username,
                password : password,
                first_name : firstName,
                last_name : lastName,
            },
            success: function (res) {
                if (res.status === 'OK' || res.result === 'OK') {
                    oRegisterResultField.innerHTML = 'Successfully registered!'
                    // Or switch back to login when register successfully
                    // switchLoginRegister('login');
                } else {
                    oRegisterResultField.innerHTML = 'User already existed!'
                }
            },
            error: function () {
                //show login error
                throw new Error('Failed to register');
            }
        })
    }

    /**
     * API Change Favorite Item
     * @param evt
     */
    function changeFavoriteItem(evt) {
        var tar = evt.target,
            oParent = tar.parentElement;
        if (oParent && oParent.className === 'fav-link') {
            console.log('change ...');
            var oCurLi = oParent.parentElement,
                classname = tar.className,
                isFavorite = classname === 'fa fa-heart' ? true : false,
                // item data <- item list + cur item index
                oItems = oItemList.getElementsByClassName('item'),
                index = Array.prototype.indexOf.call(oItems, oCurLi),
                url = './history',
                req = {
                    user_id: userId,
                    favorite: itemArr[index]
                };
            var method = !isFavorite ? 'POST' : 'DELETE';  // from fav to not fav, use DELETE, otherwise, POST
            // console.log(index);

            // call server to set / unset item as favorite
            ajax({
                method: method,
                url: url,
                data: req,
                success: function(result) {
                    // console.log(result);
                    // case 1: success:
                    if (result.status === 'OK' || result.result === 'SUCCESS') {
                        // change favorite heart icon
                        tar.className = !isFavorite ? 'fa fa-heart' : 'fa fa-heart-o';
                    } else {
                        // case 2: status not OK, not SUCCESS
                        throw new Error('Change Favorite failed! Status is not OK or SUCCESS');
                    }
                },
                error: function() {
                    throw new Error('Change Favorite failed!');
                }
            })
        }
    }

    /**
     * API Load Nearby Items
     */
    function loadNearbyData() {
        // active side bar buttons
        activeBtn('nearby-btn');
        var opt = {
            method: 'GET',
            url: './search?user_id=' + userId + '&lat=' + lat + '&lon=' + lon,
            data: null,
            message: 'nearby'
        }
        // fetch/get nearby data
        serverExecutor(opt);
    }

    /**
     * API Load Favorite Items
     */
    function loadFavoriteItems() {
        activeBtn('fav-btn');
        var opt = {
            method: 'GET',
            url: './history?user_id=' + userId,
            data: null,
            message: 'favorite'
        }

        // fetch/get nearby data
        serverExecutor(opt);
    }

    /**
     * API Load Recommended Items
     */
    function loadRecommendedItems() {
        activeBtn('recommend-btn');
        var opt = {
            method: 'GET',
            url: './recommendation?user_id=' + userId + '&lat=' + lat + '&lon=' + lon,
            data: null,
            message: 'recommended'
        }
        serverExecutor(opt);
    }

    /**
     * Render Data
     * @param data
     */
    function render(data) {
        var len = data.length,
            list = '',
            item;
        for (var i = 0; i < len; i++) {
            item = data[i];
            // console.log(item);

            list += oTpl.replace(/{{(.*?)}}/g, function(node, key) {
                // console.log(node, key);
                // case 1:
                if (key === 'location') {
                    return item[key].replace(/,/g, '<br />').replace(/\"/g, '');
                }
                // case 2:
                if (key === 'company_logo') {
                    return item[key] || 'https://via.placeholder.com/100';
                }
                // case 3:
                if (key === 'favorite') {
                    return item[key] ? "fa fa-heart" : "fa fa-heart-o";
                }
                // all other cases
                return item[key];
            });
        }
        oItemList.innerHTML = list; // add to tree (gua shu)
    }

    // by adding/removing "active" to class name to add the highlighting effect (style) of a button when on click
    function activeBtn(btnId) {
        var len = oNavBtnList.length;
        for (var i = 0; i < len; i++) {
            // removing active
            oNavBtnList[i].className = 'main-nav-btn';
        }
        var btn = document.getElementById(btnId);
        // adding active
        btn.className += ' active';
    }

    /**
     * Fetch Geolocation
     * @param cb
     */
    function initGeo(cb) {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                // lat = position.coords.latitude || lat;
                // lon = position.coords.longitude || lon;
                // lon = -122,
                // lat = 47;
                cb();
            }, function() {
                throw new Error('Geo location fetch failed!!');
            }, {
                maximumAge: 60000
            });
            oItemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i>Retrieving your location...</p>';
        } else {
            throw new Error('Your browser does not support navigator!!');
        }
    }

    function showOrHideElement(ele, style) {
        // by modifying css to hide/unhide elements on displayed page
        ele.style.display = style;
    }

    // display welcome message by hide/show some element
    function welcomeMsg(msg) {
        userId = msg.user_id || userId;
        userFullName = msg.name || userFullName;
        oWelcomeMsg.innerHTML = 'Welcome!  ' + userFullName;

        // show welcome, avatar, item area, logout btn
        showOrHideElement(oWelcomeMsg, 'block');
        showOrHideElement(oAvatar, 'block');
        showOrHideElement(oItemNav, 'block');
        showOrHideElement(oItemList, 'block');
        showOrHideElement(oLogoutBtn, 'block');

        // hide login form
        showOrHideElement(oLoginForm, 'none');
    }


    // PART4: data manager
    function fetchData() {
        // get geolocation info
        initGeo(loadNearbyData);
    }

    /**
     * Helper - AJAX
     * @param opt
     */
    function ajax(opt) {
        var opt = opt || {}, // compatibility check: if opt is not null, use it, otherwise set it to null
            method = (opt.method || 'GET').toUpperCase(),
            url = opt.url,
            data = opt.data || null,
            success = opt.success || function() {},
            error = opt.error || function() {},

            // step 1: create XMLHttpRequest object
            xhr = new XMLHttpRequest();

        // error checking
        if (!url) {
            throw new Error("missing url");
        }

        // step 2: configuration
        xhr.open(method, url, true);

        // step 3: send
        if (!data) {
            xhr.send();
        } else {
            xhr.setRequestHeader('Content-type', 'application/json;charset=utf-8');
            xhr.send(JSON.stringify(data));
        }

        // step 4: listener to listen event
        // case 1: success
        xhr.onload = function() {
            // check response：
            //FIXME: GET response is 500 ???
            if (xhr.status === 200) { // case 1: "200" response code
                // console.log(xhr.responseText);
                success(JSON.parse(xhr.responseText));
            } else { // case 2: all other abnormal cases (eg: response code != 200, or there is error)
                error();
                console.log("error here: response is not 200");
            }
        }
        // case 2: fail
        xhr.onerror = function() {
            throw new Error('The request could not be completed.');
        }
    }

    /**
     * Helper - Get Data from Server
     */
    function serverExecutor(opt) {
        oItemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i>Loading ' + opt.message + ' item...</p>';
        ajax({
            method: opt.method,
            url: opt.url,
            data: opt.data,
            success: function(result) {
                // case1: dataset is empty
                if (!result || result.length === 0) {
                    oItemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i>No ' + opt.message + ' item!</p>';
                } else { // case2: dataset is not empty, then render data
                    render(result);
                    itemArr = result;
                }
            },
            error: function () {
                throw new Error('No ' + opt.message + ' data!');
            }
        })
    }

    // start of the main program
    init();
})();


// version2:
// switch login and register (DONE)
// login API
// get data from server
// render data
// nearby favorite recommendation
// register
