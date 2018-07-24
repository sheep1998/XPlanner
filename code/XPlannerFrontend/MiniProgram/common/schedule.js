var schedule = {
    warpScheduleItems: function (scheduleItem_raw) {
        var array = [];
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
            tmp.start_date = tmp.start_time.slice(0, 10);
            tmp.completed = false;
            tmp.visible = true;
            array.push(tmp);
        }
        return array;
    }
}

module.exports = schedule;