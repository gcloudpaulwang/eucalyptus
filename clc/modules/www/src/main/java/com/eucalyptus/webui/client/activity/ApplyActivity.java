/*************************************************************************
 * Copyright 2009-2012 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 *
 * This file may incorporate work covered under the following copyright
 * and permission notice:
 *
 *   Software License Agreement (BSD License)
 *
 *   Copyright (c) 2008, Regents of the University of California
 *   All rights reserved.
 *
 *   Redistribution and use of this software in source and binary forms,
 *   with or without modification, are permitted provided that the
 *   following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE. USERS OF THIS SOFTWARE ACKNOWLEDGE
 *   THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE LICENSED MATERIAL,
 *   COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS SOFTWARE,
 *   AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *   IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA,
 *   SANTA BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY,
 *   WHICH IN THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION,
 *   REPLACEMENT OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO
 *   IDENTIFIED, OR WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT
 *   NEEDED TO COMPLY WITH ANY SUCH LICENSES OR RIGHTS.
 ************************************************************************/

package com.eucalyptus.webui.client.activity;

import static com.eucalyptus.webui.shared.checker.ValueCheckerFactory.ValueSaver;
import java.util.ArrayList;
import java.util.Arrays;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.ApplyPlace;
import com.eucalyptus.webui.client.place.LoginPlace;
import com.eucalyptus.webui.client.view.ActionResultView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.ActionResultView.ResultType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ApplyActivity extends AbstractActivity implements InputView.Presenter, ActionResultView.Presenter {

  private ApplyPlace place;
  private ClientFactory clientFactory;
  
  public static final String APPLY_ACCOUNT_CAPTION = "Apply a new account";
  public static final String APPLY_ACCOUNT_SUBJECT = "Enter new account information:";
  public static final String ACCOUNT_NAME_INPUT_TITLE = "New account name";

  public static final String APPLY_USER_CAPTION = "Apply a new user";
  public static final String APPLY_USER_SUBJECT = "Enter new user information:";
  public static final String USER_NAME_INPUT_TITLE = "New user name";
  public static final String USER_ACCOUNT_INPUT_TITLE = "Account name for new user";

  public static final String PASSWORD_RESET_CAPTION = "Request password reset";
  public static final String PASSWORD_RESET_SUBJECT = "Enter user information for password reset:";
  
  public static final String RESET_USER_INPUT_TITLE = "User name";
  public static final String RESET_USER_ACCOUNT_INPUT_TITLE = "User's account name";
  
  public static final String PASSWORD_INPUT_TITLE = "Password";
  public static final String PASSWORD2_INPUT_TITLE = "Type password again";
  public static final String EMAIL_INPUT_TITLE = "Email";
  
  public static final String APPLY_ACCOUNT_FAILURE_MESSAGE = "Failed to complete the account signup. Please contact your system administrator.";
  public static final String APPLY_ACCOUNT_SUCCESS_MESSAGE = "Account signup succeeded. Please check your email for further instructions to activate your account.";

  public static final String APPLY_USER_FAILURE_MESSAGE = "Failed to complete the user signup. Please contact your system administrator.";
  public static final String APPLY_USER_SUCCESS_MESSAGE = "User signup succeeded. Please check your email for further instructions to activate your user account.";

  public static final String PASSWORD_RESET_FAILURE_MESSAGE = "Failed to complete password reset. Please contact your system administrator.";
  public static final String PASSWORD_RESET_SUCCESS_MESSAGE = "Password reset request is sent. Please check your email for further instructions to change your password.";

  public ApplyActivity( ApplyPlace place, ClientFactory clientFactory ) {
    this.place = place;
    this.clientFactory = clientFactory;
  }
  
  @Override
  public void start( AcceptsOneWidget container, EventBus eventBus ) {
    ActionResultView view = clientFactory.getActionResultView( );
    // clear up
    view.display( ResultType.NONE, "", false );
    view.setPresenter( this );
    container.setWidget( view );
    
    switch ( place.getType( ) ) {
      case ACCOUNT:
        showApplyAccountDialog( );
        break;
      case USER:
        showApplyUserDialog( );
        break;
      case PASSWORD_RESET:
        showPasswordResetDialog( );
        break;
      default:
        cancel( null );
        break;
    }
  }

  private void showPasswordResetDialog( ) {
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( PASSWORD_RESET_CAPTION, PASSWORD_RESET_SUBJECT, new ArrayList<InputField>( Arrays.asList( new InputField( ) {

      @Override
      public String getTitle( ) {
        return RESET_USER_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createUserAndGroupNameChecker( );
      }
      
    }, new InputField( ) {
      
      @Override
      public String getTitle( ) {
        return RESET_USER_ACCOUNT_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createAccountNameChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return EMAIL_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createEmailChecker( );
      }
      
    } ) ) );
  }

  private void showApplyUserDialog( ) {
    final ValueSaver passwordSaver = ValueCheckerFactory.createValueSaver();
    final ValueChecker passwordEqualityChecker = ValueCheckerFactory.createEqualityChecker( ValueCheckerFactory.PASSWORDS_NOT_MATCH, passwordSaver );
    final InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( APPLY_USER_CAPTION, APPLY_USER_SUBJECT, new ArrayList<InputField>( Arrays.asList( new InputField( ) {

      @Override
      public String getTitle( ) {
        return USER_NAME_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createUserAndGroupNameChecker( );
      }
      
    }, new InputField( ) {
      
      @Override
      public String getTitle( ) {
        return USER_ACCOUNT_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createAccountNameChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return EMAIL_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createEmailChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.NEWPASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.checkerForAll(
            ValueCheckerFactory.createPasswordChecker( ),
            passwordSaver );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD2_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.PASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return passwordEqualityChecker;
      }
      
    } ) ) );
  }

  private void showApplyAccountDialog( ) {
    final ValueSaver passwordSaver = ValueCheckerFactory.createValueSaver();
    final ValueChecker passwordEqualityChecker = ValueCheckerFactory.createEqualityChecker( ValueCheckerFactory.PASSWORDS_NOT_MATCH, passwordSaver );
    final InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( APPLY_ACCOUNT_CAPTION, APPLY_ACCOUNT_SUBJECT, new ArrayList<InputField>( Arrays.asList( new InputField( ) {

      @Override
      public String getTitle( ) {
        return ACCOUNT_NAME_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createAccountNameChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return EMAIL_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createEmailChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.NEWPASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.checkerForAll(
            ValueCheckerFactory.createPasswordChecker( ),
            passwordSaver );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD2_INPUT_TITLE;
      }

      @Override
      public ValueType getType( ) {
        return ValueType.PASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return passwordEqualityChecker;
      }
      
    } ) ) );
  }

  @Override
  public void process( String subject, ArrayList<String> values ) {
    if ( APPLY_ACCOUNT_SUBJECT.equals( subject ) ) {
      doApplyAccount( values.get( 0 ), values.get( 1 ), values.get( 2 ) );
    } else if ( APPLY_USER_SUBJECT.equals( subject ) ) {
      doApplyUser( values.get( 0 ), values.get( 1 ), values.get( 2 ), values.get( 3 ) );
    } else if ( PASSWORD_RESET_SUBJECT.equals( subject ) ) {
      doResetPassword( values.get( 0 ), values.get( 1 ), values.get( 2 ) );
    }
  }

  private void doResetPassword( String userName, String accountName, String email ) {
    clientFactory.getActionResultView( ).loading( );
    
    clientFactory.getBackendService( ).requestPasswordRecovery( userName, accountName, email, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        clientFactory.getActionResultView( ).display( ResultType.ERROR, PASSWORD_RESET_FAILURE_MESSAGE, true );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getActionResultView( ).display( ResultType.INFO, PASSWORD_RESET_SUCCESS_MESSAGE, true );
      }
      
    } );
  }

  private void doApplyUser( String userName, String accountName, String email, String password ) {
    clientFactory.getActionResultView( ).loading( );
    
    clientFactory.getBackendService( ).signupUser( userName, accountName, password, email, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        clientFactory.getActionResultView( ).display( ResultType.ERROR, APPLY_USER_FAILURE_MESSAGE, true );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getActionResultView( ).display( ResultType.INFO, APPLY_USER_SUCCESS_MESSAGE, true );
      }
      
    } );
  }

  private void doApplyAccount( String accountName, String email, String password ) {
    clientFactory.getActionResultView( ).loading( );
    
    clientFactory.getBackendService( ).signupAccount( accountName, password, email, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        clientFactory.getActionResultView( ).display( ResultType.ERROR, APPLY_ACCOUNT_FAILURE_MESSAGE, true );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getActionResultView( ).display( ResultType.INFO, APPLY_ACCOUNT_SUCCESS_MESSAGE, true );
      }
      
    } );
  }

  @Override
  public void cancel( String subject ) {
    clientFactory.getLifecyclePlaceController( ).goTo( new LoginPlace( LoginPlace.DEFAULT_PROMPT ) );
  }

  @Override
  public void onConfirmed( ) {
    // Go back
    cancel( null );
  }
  
}
