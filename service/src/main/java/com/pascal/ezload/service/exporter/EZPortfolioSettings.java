/**
 * ezService - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.service.exporter;

import com.pascal.ezload.service.config.SettingsManager;
import com.pascal.ezload.common.util.Checkable;
import com.pascal.ezload.common.util.StringValue;

public class EZPortfolioSettings extends Checkable<EZPortfolioSettings> {

    public enum Field {ezPortfolioUrl}

    private String ezPortfolioUrl;

    public String getEzPortfolioUrl() {
        return ezPortfolioUrl;
    }

    public void setEzPortfolioUrl(String ezPortfolioUrl) {
        this.ezPortfolioUrl = ezPortfolioUrl == null ? null : ezPortfolioUrl.trim();
    }

    @Override
    public EZPortfolioSettings validate() {
        new StringValue(this, Field.ezPortfolioUrl.name(), ezPortfolioUrl).checkRequired().checkPrefixMatch(SettingsManager.EZPORTFOLIO_GDRIVE_URL_PREFIX);
        return this;
    }

}
