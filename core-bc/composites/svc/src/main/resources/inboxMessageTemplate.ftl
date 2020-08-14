<#setting locale="sv_SE">
<?xml version="1.0"?>
<article>

    <section>
        <title>Beställda produkter</title>
    </section>

<#list deliveryChoiceMappedToOrderRows as choice, itemList>
    <section>
<#list itemList as item>
        <variablelist>
            <varlistentry>
                <term>&nbsp;</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>Produktgrupp:</term>
                <listitem>${item.article.productArea.originalText?capitalize}</listitem>
            </varlistentry>
            <varlistentry>
                <term>Artikel:</term>
                <listitem>${item.article.articleName}</listitem>
            </varlistentry>
            <varlistentry>
                <term>Artikelnr.:</term>
                <listitem>${item.article.articleNo}</listitem>
            </varlistentry>
            <varlistentry>
<#if item.article.packageSize == 1>
                <term>Antal artiklar:</term>
                <listitem>${item.noOfPcs}</listitem>
<#else>
                <term>Antal förpackningar:</term>
                <listitem>${item.noOfPackages}</listitem>
</#if>
            </varlistentry>
        </variablelist>
</#list>
    </section>

    <section>
        <title>Leveransinformation</title>
        <variablelist>
            <varlistentry>
                <term>&nbsp;</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.deliveryMethod.name() == 'UTLÄMNINGSSTÄLLE' && choice.getDeliveryPoint()??>
            <varlistentry>
                <term></term>
                <listitem>Utlämningsställe:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.deliveryPoint.deliveryPointName}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.deliveryPoint.deliveryPointAddress}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.deliveryPoint.deliveryPointPostalCode} ${choice.deliveryPoint.deliveryPointCity}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#elseif choice.deliveryMethod.name() == 'UTLÄMNINGSSTÄLLE' && choice.getDeliveryPoint()!??>
            <varlistentry>
                <term></term>
                <listitem>Närmaste utlämningsställe för:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.receiver}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.homeDeliveryAddress.careOfAddress?has_content>
            <varlistentry>
                <term>c/o ${choice.homeDeliveryAddress.careOfAddress}</term>
                <listitem> </listitem>
            </varlistentry>
</#if>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.street}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.postalCode} ${choice.homeDeliveryAddress.city}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.deliveryComment??>
            <varlistentry>
                <term></term>
                <listitem>Leveranskommentar:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.deliveryComment}:</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
</#if>
<#else >
            <varlistentry>
                <term></term>
                <listitem>Leveransadress:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.receiver}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.homeDeliveryAddress.careOfAddress?has_content>
            <varlistentry>
                <term>c/o ${choice.homeDeliveryAddress.careOfAddress}</term>
                <listitem> </listitem>
            </varlistentry>
</#if>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.street}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.postalCode} ${choice.homeDeliveryAddress.city}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.deliveryComment??>
            <varlistentry>
                <term></term>
                <listitem>Leveranskommentar:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.deliveryComment}:</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
</#if>
</#if>
<#if choice.contactPerson??>
            <varlistentry>
                <term></term>
                <listitem>Kontaktperson:</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.contactPerson}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
</#if>
        </variablelist>

<#if choice.deliveryNotificationMethod??>
        <variablelist>
            <varlistentry>
                <term>&nbsp;</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
        </variablelist>

        <variablelist>
            <varlistentry>
                <term></term>
                <listitem>${choice.deliveryNotificationMethod.value.value()?capitalize}-avisering:</listitem>
            </varlistentry>
<#if choice.deliveryNotificationMethod.value.name() == 'BREV'>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.receiver!}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.street!}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.homeDeliveryAddress.postalCode!} ${choice.homeDeliveryAddress.city!}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#else>
            <varlistentry>
                <term>${choice.deliveryNotificationReceiver}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
</#if>
        </variablelist>
        <variablelist>
            <varlistentry>
                <term>&nbsp;</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
        </variablelist>
</#if>
        <variablelist>
            <varlistentry>
                <term>___________________________________________________________________________</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
        </variablelist>
    </section>
<#if choice.getInvoiceAddress()??>
    <section>
        <title>Fakturaadress:</title>
        <variablelist>
            <varlistentry>
                <term>${choice.invoiceAddress.receiver}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
<#if choice.invoiceAddress.careOfAddress?has_content>
            <varlistentry>
                <term>c/o ${choice.invoiceAddress.careOfAddress}</term>
                <listitem> </listitem>
            </varlistentry>
</#if>
            <varlistentry>
                <term>${choice.invoiceAddress.street}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
            <varlistentry>
                <term>${choice.invoiceAddress.postalCode} ${choice.invoiceAddress.city}</term>
                <listitem>&nbsp;</listitem>
            </varlistentry>
        </variablelist>
    </section>
</#if>
</#list>

</article>
