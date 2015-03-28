package org.jenerate.internal.strategy.method.content.impl.commonslang;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.jenerate.internal.domain.data.ToStringGenerationData;
import org.jenerate.internal.domain.preference.impl.JeneratePreference;
import org.jenerate.internal.strategy.method.content.MethodContentStrategyIdentifier;
import org.jenerate.internal.strategy.method.skeleton.impl.ToStringMethodSkeleton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Junit test for the {@link CommonsLangToStringMethodContent}
 * 
 * @author maudrain
 */
@RunWith(MockitoJUnitRunner.class)
public class CommonsLangToStringMethodContentTest extends
        AbstractMethodContentTest<CommonsLangToStringMethodContent, ToStringMethodSkeleton, ToStringGenerationData> {

    private static final String TO_STRING_CACHING_FIELD = "toString";

    @Mock
    private IField cachingField;

    @Override
    public ToStringGenerationData getConcreteData() {
        return mock(ToStringGenerationData.class);
    }

    @Override
    public void callbackAfterSetUp() throws Exception {
        when(data.getToStringStyle()).thenReturn(CommonsLangToStringStyle.NO_STYLE);
        mockCacheToString(false);
        when(objectClass.getField(anyString())).thenReturn(cachingField);
        when(cachingField.exists()).thenReturn(false);
        when(preferencesManager.getCurrentPreferenceValue(JeneratePreference.TOSTRING_CACHING_FIELD)).thenReturn(
                TO_STRING_CACHING_FIELD);
        methodContent = new CommonsLangToStringMethodContent(MethodContentStrategyIdentifier.USE_COMMONS_LANG,
                preferencesManager);
    }

    @Test
    public void testGetMethodContentStrategyIdentifier() {
        assertEquals(MethodContentStrategyIdentifier.USE_COMMONS_LANG,
                methodContent.getMethodContentStrategyIdentifier());
    }

    @Test
    public void testGetRelatedMethodSkeletonClass() {
        assertEquals(ToStringMethodSkeleton.class, methodContent.getRelatedMethodSkeletonClass());
    }

    @Test
    public void testGetLibrariesToImportWithCommonsLangAndNoStyle() {
        Set<String> librariesToImport = methodContent.getLibrariesToImport(data);
        assertEquals(1, librariesToImport.size());
        assertEquals(CommonsLangMethodContentLibraries.getToStringBuilderLibrary(false), librariesToImport.iterator()
                .next());
    }

    @Test
    public void testGetLibrariesToImportWithCommonsLangAndStyle() {
        when(data.getToStringStyle()).thenReturn(CommonsLangToStringStyle.MULTI_LINE_STYLE);
        Set<String> librariesToImport = methodContent.getLibrariesToImport(data);
        assertEquals(2, librariesToImport.size());
        Iterator<String> iterator = librariesToImport.iterator();
        assertEquals(CommonsLangMethodContentLibraries.getToStringBuilderLibrary(false), iterator.next());
        assertEquals(CommonsLangToStringStyle.getToStringStyleLibrary(false), iterator.next());
    }

    @Test
    public void testGetLibrariesToImportWithCommonsLang3AndNoStyle() {
        methodContent = new CommonsLangToStringMethodContent(MethodContentStrategyIdentifier.USE_COMMONS_LANG3,
                preferencesManager);
        Set<String> librariesToImport = methodContent.getLibrariesToImport(data);
        assertEquals(1, librariesToImport.size());
        assertEquals(CommonsLangMethodContentLibraries.getToStringBuilderLibrary(true), librariesToImport.iterator()
                .next());
    }

    @Test
    public void testGetLibrariesToImportWithCommonsLang3AndStyle() {
        when(data.getToStringStyle()).thenReturn(CommonsLangToStringStyle.MULTI_LINE_STYLE);
        methodContent = new CommonsLangToStringMethodContent(MethodContentStrategyIdentifier.USE_COMMONS_LANG3,
                preferencesManager);
        Set<String> librariesToImport = methodContent.getLibrariesToImport(data);
        assertEquals(2, librariesToImport.size());
        Iterator<String> iterator = librariesToImport.iterator();
        assertEquals(CommonsLangMethodContentLibraries.getToStringBuilderLibrary(true), iterator.next());
        assertEquals(CommonsLangToStringStyle.getToStringStyleLibrary(true), iterator.next());
    }

    @Test
    public void testGetMethodContentDefault() throws Exception {
        String content = methodContent.getMethodContent(objectClass, data);
        assertEquals("return new ToStringBuilder(this).append(\"field1\", field1)"
                + ".append(\"field2\", field2).toString();\n", content);
    }

    @Test
    public void testGetMethodWithCachingFieldNotAlreadyPresent() throws Exception {
        mockCacheToString(true);
        String content = methodContent.getMethodContent(objectClass, data);
        verify(objectClass, times(1)).createField("private transient String " + TO_STRING_CACHING_FIELD + ";\n\n",
                elementPosition, true, null);
        assertEquals("if (toString== null) {\ntoString = new ToStringBuilder(this).append(\"field1\", field1)"
                + ".append(\"field2\", field2).toString();\n}\nreturn toString;\n", content);
    }

    @Test
    public void testGetMethodWithCachingFieldAlreadyPresent() throws Exception {
        when(cachingField.exists()).thenReturn(true);
        mockCacheToString(true);
        String content = methodContent.getMethodContent(objectClass, data);
        verify(cachingField, times(1)).delete(true, null);
        verify(objectClass, times(1)).createField("private transient String " + TO_STRING_CACHING_FIELD + ";\n\n",
                elementPosition, true, null);
        assertEquals("if (toString== null) {\ntoString = new ToStringBuilder(this).append(\"field1\", field1)"
                + ".append(\"field2\", field2).toString();\n}\nreturn toString;\n", content);
    }

    @Test
    public void testGetMethodContentWithStyle() throws Exception {
        when(data.getToStringStyle()).thenReturn(CommonsLangToStringStyle.SHORT_PREFIX_STYLE);
        String content = methodContent.getMethodContent(objectClass, data);
        assertEquals("return new ToStringBuilder(this, CommonsLangToStringStyle.SHORT_PREFIX_STYLE)"
                + ".append(\"field1\", field1).append(\"field2\", field2).toString();\n", content);
    }

    @Test
    public void testGetMethodContentWithAppendSuper() throws Exception {
        when(data.getAppendSuper()).thenReturn(true);
        String content = methodContent.getMethodContent(objectClass, data);
        assertEquals("return new ToStringBuilder(this).appendSuper(super.toString())"
                + ".append(\"field1\", field1).append(\"field2\", field2).toString();\n", content);
    }

    @Test
    public void testGetMethodContentWithUseGettersInsteadOfFields() throws Exception {
        when(data.getUseGettersInsteadOfFields()).thenReturn(true);
        String content = methodContent.getMethodContent(objectClass, data);
        assertEquals("return new ToStringBuilder(this).append(\"field1\", isField1())"
                + ".append(\"field2\", getField2()).toString();\n", content);
    }

    private void mockCacheToString(boolean cacheToString) throws Exception {
        when(preferencesManager.getCurrentPreferenceValue(JeneratePreference.CACHE_TOSTRING)).thenReturn(cacheToString);
        when(field1.getFlags()).thenReturn(cacheToString ? 16 : 0);
        when(field2.getFlags()).thenReturn(cacheToString ? 16 : 0);
    }
}
