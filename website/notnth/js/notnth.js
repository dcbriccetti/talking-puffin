"use strict";

function NotNth() {
    this.done = false;
    this.nextFollower = 1;
    this.numFollowers = 0;
    this.cycle = 0;
}

NotNth.followProbability = 0.8;
NotNth.unfollowProbability = 0.5;
NotNth.goal = 25;
NotNth.animationDelayMs = 100;

NotNth.prototype = {

    notifyCycle: function (cycle) {
        $('#cycle').empty().append(cycle);
    },

    notify: function (msg) {
        $('#event').empty().append(msg);
    },

    setNumFol: function () {
        $('#numFollowers').empty().append(this.numFollowers);
    },

    addFollower: function () {
        this.notify("Person " + this.nextFollower + " is following");
        var newFollower =
            $('<div class="fol" id="f' + this.nextFollower + '">Person ' + this.nextFollower + '</div>').css("display", "none");
        if (this.nextFollower == NotNth.goal) {
            newFollower.addClass("followerAtGoal")
        }
        $("#followers").append(newFollower);
        newFollower.slideDown('fast');
        ++this.nextFollower;
        ++this.numFollowers;
        this.setNumFol();
    },

    unfollow: function () {
        var followers = $('#followers').children();
        var fl = followers.length;
        if (followers.length > 0) {
            var unfollower = $(followers[Math.floor(Math.random() * followers.length)]);
            this.notify(unfollower[0].innerHTML + " is unfollowing");
            --this.numFollowers;
            var nn = this;
            unfollower.slideUp('fast', function () {
                $("#ffollowers").append(unfollower);
                nn.setNumFol();
                unfollower.slideDown('fast');
            });
        }
    },

    iterate: function () {
        this.notifyCycle(++this.cycle);
        var activityThisCycle = false;
        if (Math.random() < NotNth.followProbability) {
            activityThisCycle = true;
            this.addFollower();
            if (this.numFollowers >= NotNth.goal) {
                var thisFollower = (this.nextFollower - 1);
                this.notify("Hurray! Person " + thisFollower +
                    " is the " + NotNth.goal + "th follower!" +
                    (NotNth.goal == thisFollower ? "" :
                        " (But not so! It was Person " + NotNth.goal + " some time ago.)")
                );
                this.done = true;
            }
        }
        if (!this.done) {
            if (this.numFollowers > 0 && Math.random() < NotNth.unfollowProbability) {
                activityThisCycle = true;
                this.unfollow();
            }
            var nn = this;
            setTimeout(function() {nn.iterate()}, NotNth.animationDelayMs);
        }
        if (!activityThisCycle) {
            this.notify("No activity")
        }
    },

    resetDom: function () {
        $('#followers').empty();
        $('#ffollowers').empty();
    },

    run: function() {
        var fp = $('#followProb');
        this.resetDom();
        this.setNumFol();
        this.iterate();
    }
};
