package com.ocado.plus.api.circlesync;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class ActiveDirectoryLoader implements SourceLoader {
  private static String sp = "com.sun.jndi.ldap.LdapCtxFactory";
  private static String ldapUrl = "<Your LDAP URL here>";
  private static String user = "<Your LDAP connect user here>";
  private static String password = "<Your LDAP connect password here>";
  private static String base = "<Your LDAP domain user base here>"; // e.g. "OU=Users,DC=example,DC=com"

  @Override
  public List<String> getMembersForGroup(String groupName) throws Exception {
    Hashtable<String,String> env = new Hashtable<String,String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, sp);
    env.put(Context.PROVIDER_URL, ldapUrl);
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, password);
    SearchControls sc = new SearchControls();
    String[] attributeFilter = {"mail"};
    sc.setReturningAttributes(attributeFilter);
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    String filter = "(&(objectCategory=user)(memberOf="+groupName+"))";
    DirContext dctx = null;
    NamingEnumeration<SearchResult> results = null;
    List<String> emailAddresses = new ArrayList<String>();

    try {
      dctx = new InitialDirContext(env);
      results = dctx.search(base, filter, sc);
      while (results.hasMore()) {
        SearchResult sr = results.next();
        Attributes attrs = sr.getAttributes();
        NamingEnumeration<?> values = attrs.getAll();
        while (values.hasMore()) {
          Attribute attr = (Attribute)values.next();
          NamingEnumeration<?> items = attr.getAll();
          while (items.hasMore()) {
            try {
              String item = items.next().toString();
              emailAddresses.add(item);
            } catch (Exception e) {
            }
          }
        }
      }
    } finally {
      if (results != null) {
        try {
          results.close();
        } catch (Throwable t) {
        }
      }
      if (dctx != null) {
        try {
          dctx.close();
        } catch (Exception e) {
        }
      }
    }

    return emailAddresses;
  }
}
