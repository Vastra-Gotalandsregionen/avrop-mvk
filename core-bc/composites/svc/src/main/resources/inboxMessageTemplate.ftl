<?xml version="1.0"?>
<article>
    <info>
        <title>Beställda produkter</title>
    </info>

    <section>
    <#list prescriptionItems as item>
        <variablelist>
            <varlistentry>
                <term>Produktgrupp:</term>
                <listitem>${item.article.productArea}</listitem>
            </varlistentry>
            <varlistentry>
                <term></term>
                <listitem>${item.article.articleName}</listitem>
            </varlistentry>
            <varlistentry>
                <term>Artikelnr.:</term>
                <listitem>${item.article.articleNo}</listitem>
            </varlistentry>
        </variablelist>
    </#list>
    </section>

    <section>
        <title>Leveransinformation</title>
    <#list deliveryChoices as choice>
        <para>${choice.deliveryMethod}:</para>
        <variablelist>
            <#if choice.deliveryMethod.name() == 'UTLÄMNINGSSTÄLLE'>
                <varlistentry>
                    <term>${choice.deliveryPoint.deliveryPointName}</term>
                    <listitem></listitem>
                </varlistentry>
                <varlistentry>
                    <term>${choice.deliveryPoint.deliveryPointAddress}</term>
                    <listitem></listitem>
                </varlistentry>
                <varlistentry>
                    <term>${choice.deliveryPoint.deliveryPointPostalCode} ${choice.deliveryPoint.deliveryPointCity}</term>
                    <listitem></listitem>
                </varlistentry>
            <#else >
                <varlistentry>
                    <term>${choice.homeDeliveryAddress.receiver}</term>
                    <listitem></listitem>
                </varlistentry>
                <varlistentry>
                    <term>${choice.homeDeliveryAddress.streetAddress}</term>
                    <listitem></listitem>
                </varlistentry>
                <varlistentry>
                    <term>${choice.homeDeliveryAddress.postalCode}${choice.homeDeliveryAddress.city}</term>
                    <listitem></listitem>
                </varlistentry>
            </#if>

        </variablelist>
    </#list>

    </section>

    </variablelist>
</article>
