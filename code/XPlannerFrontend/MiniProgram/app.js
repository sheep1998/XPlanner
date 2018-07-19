var extensions = require("/data/extensions");
var userInfo = require("/data/userInfo");
var scheduleItems = require("/data/scheduleItem");

function warpExtensions(extensions_raw) {
  var result = [];
  for (var i = 0; i < extensions_raw.length; i++) {
    var tmp = new Object();
    tmp.id = extensions_raw[i].planner_id;
    tmp.name = extensions_raw[i].planner_name;
    tmp.description = extensions_raw[i].description;
    tmp.content = "尚无内容。";
    tmp.visible = false;
    tmp.messages = extensions_raw[i].planner_name == "Spider" ? 8 : -1;
    tmp.icon = extensions_raw[i].picture_path_name;
    var name = tmp.name.toLowerCase();
    tmp.extension_path = "/extensions/" + name + "/" + name;
    result.push(tmp);
  }
  return result;
}

function warpScheduleItems(scheduleItem_raw) {
  var result = [];
  for (var i = 0; i < scheduleItem_raw.length; i++) {
    var tmp = new Object();
    tmp.title = scheduleItem_raw[i].title;
    tmp.start_time = scheduleItem_raw[i].start_time;
    tmp.end_time = scheduleItem_raw[i].end_time;
    tmp.description = scheduleItem_raw[i].description;
    tmp.address = scheduleItem_raw[i].address;
    tmp.scheduleItem_id = scheduleItem_raw[i].scheduleItem_id;
    tmp.user_id = scheduleItem_raw[i].user_id;
    tmp.start_concret_time = tmp.start_time.slice(11, 16);
    tmp.end_concret_time = tmp.end_time.slice(11, 16);
    // console.log(tmp.start_concret_time);
    result.push(tmp);
  }
  console.log("Global scheduleItems");
  console.log(result);
  return result;
}

//app.js
App({
  onLaunch: function() {
    // 展示本地存储能力
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      }
    });
    // 获取用户信息
    wx.getSetting({
      success: res => {
        if (res.authSetting['scope.userInfo']) {
          // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
          wx.getUserInfo({
            success: res => {
              // 可以将 res 发送给后台解码出 unionId
              this.globalData.userInfo = res.userInfo

              // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
              // 所以此处加入 callback 以防止这种情况
              if (this.userInfoReadyCallback) {
                this.userInfoReadyCallback(res)
              }
            }
          })
        }
      }
    });

    /* 向后端发送选择的日期，获取当月有日程的日期的数组和当天的日期，设置globalData，注意要使用包装函数 */
    /* 另外，使用wx.getStorage获取存储的选区的日期，也需要设置globalData */

  },
  globalData: {
    date: "2018-07-18",
    userInfo: userInfo,
    extensions: warpExtensions(extensions),
    userFoodEaten: [],
    scheduleItems: warpScheduleItems(scheduleItems),
    // dayWithItems: {17: [1, 0], 18: [3, 0], 20: [2, 0]},
  }
})