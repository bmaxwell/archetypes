

add-user.sh to ApplicationRealm
ejbuser / redhat1!

cp standalone-full-ha.xml $JBOSS_HOME/standalone/configuration
cp -R standalone node1
cp -R standalone node2


mvn clean install -s settings.xml -DjbossHome=jboss-eap-7.1
