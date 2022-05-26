/*
 * ezClient - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
//window.$ = window.jQuery = require('jquery');
import $ from "jquery";

export class DynamicLogger {
    constructor(){
        this.idQueue = [];
        this.lastId = 1;
        this.idQueue.push(this.lastId);
        this.stopped = false;
        this.openIcon = '<span class="toggler">+</span>';
        this.closeIcon = '<span class="toggler">-</span>';
    }


    isStopped(){
        return this.stopped;
    }

    stop(){
        this.stopped = true;
    }

    updateIcons(objDiv) {
        if (objDiv.hasClass('openedItem')) {
            objDiv.find('span.toggler').replaceWith(this.closeIcon);
        } else {
            objDiv.find('span.toggler').replaceWith(this.openIcon);
        }
    }

    closeAll(objDiv) {
        var items = objDiv.parent().siblings().find('ul.submenu');
        var self = this;
        items.each(function () {
            var ul = $(this).slideUp('slow').parent();
            ul.find('span.toggler').replaceWith(self.openIcon);
            ul.find('a').first().removeClass('openedItem');
        });
    }

    pushSection(elem){
        var parentId = this.idQueue[this.idQueue.length-1];
        this.lastId++;
        this.idQueue.push(this.lastId);
        $('#'+parentId).append("<li class='section'><div href='#'>"+elem+this.openIcon+"</div><ul class='submenu' id="+this.lastId+"></ul></li>");
        var a = $('#'+parentId).children("li").last().children("div").first();
        this.show(a);
        var self = this;
        function handler(){
            self.openOrClose(a);
        }
        a.click(handler);
    }

    add(elem, isError){
        var parentId = this.idQueue[this.idQueue.length-1];
        $('#'+parentId).append("<li class='log'><div href='#' "+(isError? "class='error'" : "")+">"+elem+"</div></li>");
        if (isError){
            for (let i = 0; i < this.idQueue.length; i++){
                $('#'+this.idQueue[i]).parent().children('div').first().addClass('error');
            }
        }
    }

    openOrClose(objDiv){
        var leaveOpen = false; // set it to true to not close all others div when clicking
        if (leaveOpen === false) {
            this.closeAll(objDiv);
        }

        if (objDiv.hasClass('openedItem')) {
            this.hide(objDiv);
        }
        else{
            this.show(objDiv);
        }

        this.updateIcons(objDiv);
    }

    hide(objDiv){
        objDiv.removeClass('openedItem');
        objDiv.siblings('ul').first().slideUp();
        this.updateIcons(objDiv);
    }

    show(objDiv){
        objDiv.addClass('openedItem');
        objDiv.siblings('ul').first().slideDown();
        this.updateIcons(objDiv);
    }

    popSection(){
        var sectionId = this.idQueue.pop();
        this.hide($('#'+sectionId).parent().children('div').first());
    }

}