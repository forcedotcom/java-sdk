$!

    Copyright (c) 2011, salesforce.com, inc.
    All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, are permitted provided
    that the following conditions are met:

       Redistributions of source code must retain the above copyright notice, this list of conditions and the
       following disclaimer.

       Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
       the following disclaimer in the documentation and/or other materials provided with the distribution.

       Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
       promote products derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
    TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
    HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

!$

package $packageName; separator="."$;

import javax.persistence.*;

/**
 * Java model for the Force.com $objectInfo.name$ object.
 *
 * For this class, the following operations are available
 * via Force.com JPA:
 *
 *     Create : $objectInfo.createable$
 *     Read   : $objectInfo.queryable$
 *     Update : $objectInfo.updateable$
 *     Delete : $objectInfo.deletable$
 *
 * This class was auto-generated. It is considered owned
 * by the Force.com database.
 *
 * @author $userInfo.userFullName$ ($userInfo.userEmail$)
 **/
$objectInfo; format="classAnnotation"$
public class $objectInfo; format="className"$ extends $objectInfo; format="superClassName"$ {

    public static final String KEY_PREFIX = "$objectInfo.keyPrefix$";

    $fields:{f | 
    $f; format="fieldComments1"$
    protected $f; format="fieldType"$ $f; format="fieldName"$;}$
    
    $fields:{f | 
    $f; format="getterAnnotation1"$
    public $f; format="fieldType"$ get$f; format="methodName"$() \{
        return this.$f; format="fieldName"$;
    \}
    
    $f; format="setterAnnotation1"$
    public void set$f; format="methodName"$($f; format="fieldType"$ $f; format="fieldName"$) \{
        this.$f; format="fieldName"$ = $f; format="fieldName"$;
    \}
    }$
    
$fields: {f |
    $if(f.picklistValues)$
        $templates/PicklistValueEnum(field=f)$
    
    $endif$
}$
}