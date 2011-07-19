#!/bin/sh
#
# Copyright (c) 2011, salesforce.com, inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification, are permitted provided
# that the following conditions are met:
#
#    Redistributions of source code must retain the above copyright notice, this list of conditions and the
#    following disclaimer.
#
#    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
#    the following disclaimer in the documentation and/or other materials provided with the distribution.
#
#    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
#    promote products derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
# PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
# TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#

# Install pre-commit hook
echo "Installing pre-commit hook..."
cp build-tools/scripts/pre-commit.sh .git/hooks
mv .git/hooks/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
echo "Installed."

# Mark test org properties file as writable
TEST_ORG_PROPERTIES_FILE=javasdk-test/qa-utils/src/main/resources/force-sdk-test.properties
echo
echo "Marking test org properties file as writable..."
git update-index --assume-unchanged $TEST_ORG_PROPERTIES_FILE
echo "Marked."

# Enable git flow
# Check if git flow configuration already exists
GITFLOW_CONFIG=$(grep gitflow .git/config)
if [ -z "$GITFLOW_CONFIG" ]; then
  echo
  echo "Enabling git flow..."
  git checkout master
  git checkout develop
  if command -v git-flow &>/dev/null; then
    git flow init
    echo "git flow enabled."
  else
    echo "**git flow not installed. Please install and initialize."
  fi
fi

# Setup test org
echo
read -p "Would you like to interactively link to a test org [y/N]? " answer
if [[ "$answer" == "y" || "$answer" == "Y" ]]; then
  echo
  ENDPOINT_DEFAULT=$(grep ^endpoint= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  read -p "On which server is your test org located (e.g. login.salesforce.com) [$ENDPOINT_DEFAULT]? " endpoint
  if [ -z "$endpoint" ]; then
    endpoint=$ENDPOINT_DEFAULT
  fi

  ENDPOINT_PROTOCOL_DEFAULT=$(grep ^endpoint\.protocol= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  if [ -z "$ENDPOINT_PROTOCOL_DEFAULT" ]; then
    ENDPOINT_PROTOCOL_DEFAULT=https
  fi
  read -p "What protocol should we use to connect to that server [$ENDPOINT_PROTOCOL_DEFAULT]? " endpoint_protocol
  if [ -z "$endpoint_protocol" ]; then
    endpoint_protocol=$ENDPOINT_PROTOCOL_DEFAULT
  fi

  FORCE_API_VERSION_DEFAULT=$(grep ^force\.apiVersion= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  if [ -z "$FORCE_API_VERSION_DEFAULT" ]; then
    FORCE_API_VERSION_DEFAULT=$(grep \<force.version.min\>.*\<\/force.version.min\> pom.xml | grep -E -o [0-9]+.[0-9]+)
  fi
  read -p "Which Force.com API version would you like to test against [$FORCE_API_VERSION_DEFAULT]? " force_apiVersion
  if [ -z "$force_apiVersion" ]; then
    force_apiVersion=$FORCE_API_VERSION_DEFAULT
  fi

  echo
  echo "Please provide the username and password for the test org."
  USER_DEFAULT=$(grep ^user= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  read -p "Username [$USER_DEFAULT]: " user
  if [ -z "$user" ]; then
    user=$USER_DEFAULT
  fi

  read -s -p "Password: " password
  echo
  read -s -p "Retype password: " retype_password
  echo

  password_tries=1
  while [[ "$password" != "$retype_password" &&  $password_tries < 3 ]]; do
    echo
    echo "Failed to match passwords. Please try again."

    read -s -p "Password: " password
    echo
    read -s -p "Retype password: " retype_password
    echo

    let "password_tries += 1"
  done

  if [ "$password" != "$retype_password" ]; then
    echo
    echo "**Exceeded number of password tries. Please add password to $TEST_ORG_PROPERTIES_FILE"
    password=
  fi

  echo
  echo "Developer Edition Force.com orgs can carry a namespace. These can be created under Setup > Create > Packages."
  echo "If the test org has a Force.com namespace, please enter it below. If no namespace exists or has been created, then simple hit [ENTER]."
  FORCE_NAMESPACE_DEFAULT=$(grep ^force\.namespace= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  read -p "Force.com namespace [$FORCE_NAMESPACE_DEFAULT]: " force_namespace
  if [ -z "$force_namespace" ]; then
    force_namespace=$FORCE_NAMESPACE_DEFAULT
  fi

  echo
  echo "For certain tests to run, it is required to create an OAuth Consumer in the test org."
  echo "Please create a record under Setup > Develop > Remote Access."
  echo "**The callback URL *must* be: http://localhost:8888/force-app/_auth"
  echo "**This option *must* be selected: No user approval required for users in this organization"
  echo
  echo "Once created, please enter the key and secret below."
  FORCE_TEST_OAUTH_KEY_DEFAULT=$(grep ^force\.test\.oauth\.key= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  read -p "OAuth key [$FORCE_TEST_OAUTH_KEY_DEFAULT]: " force_test_oauth_key
  if [ -z "$force_test_oauth_key" ]; then
    force_test_oauth_key=$FORCE_TEST_OAUTH_KEY_DEFAULT
  fi

  FORCE_TEST_OAUTH_SECRET_DEFAULT=$(grep ^force\.test\.oauth\.secret= $TEST_ORG_PROPERTIES_FILE | cut -d '=' -f2)
  read -p "OAuth secret [$FORCE_TEST_OAUTH_SECRET_DEFAULT]: " force_test_oauth_secret
  if [ -z "$force_test_oauth_secret" ]; then
    force_test_oauth_secret=$FORCE_TEST_OAUTH_SECRET_DEFAULT
  fi

  sed -i s/^endpoint=.*/endpoint=$endpoint/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^endpoint.protocol=.*/endpoint.protocol=$endpoint_protocol/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^force.apiVersion=.*/force.apiVersion=$force_apiVersion/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^user=.*/user=$user/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^password=.*/password=$password/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^force.namespace=.*/force.namespace=$force_namespace/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^force.test.oauth.key=.*/force.test.oauth.key=$force_test_oauth_key/g $TEST_ORG_PROPERTIES_FILE
  sed -i s/^force.test.oauth.secret=.*/force.test.oauth.secret=$force_test_oauth_secret/g $TEST_ORG_PROPERTIES_FILE
  
  echo
  echo "Values written to $TEST_ORG_PROPERTIES_FILE"
fi

