/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class QuestionReplyException extends SilverpeasException {

  // constructors
  /**
   * Constructor which calls the super constructor
   * @param callingClass (String) the name of the module which catchs the Exception
   * @param errorLevel (int) the level error of the exception
   * @param message (String) the level of the exception label
   * @param extraParams (String) the generic exception message
   * @param nested (Exception) the exception catched
   */
  public QuestionReplyException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public QuestionReplyException(String callingClass, int errorLevel,
      String message, String extraParams) {
    this(callingClass, errorLevel, message, extraParams, null);
  }

  public QuestionReplyException(String callingClass, int errorLevel,
      String message, Exception nested) {
    this(callingClass, errorLevel, message, "", nested);
  }

  public QuestionReplyException(String callingClass, int errorLevel,
      String message) {
    this(callingClass, errorLevel, message, "", null);
  }

  //
  // public methods
  //

  /**
   * Returns the name of this jobPeas
   * @return the name of this module
   */
  public String getModule() {
    return "QuestionReply";
  }

}
